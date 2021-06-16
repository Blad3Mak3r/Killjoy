/*******************************************************************************
 * Copyright (c) 2021. Blademaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package dev.killjoy.apis.news

import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object NewsRetriever {
    private var cachedNews: AtomicReference<List<ValorantNew>?> = AtomicReference(null)
    private var cachedNewsTimestamp: Long = 0L
    private val logger = LoggerFactory.getLogger(NewsRetriever::class.java)

    val cached: Boolean
        get() = cachedNews.get() != null && cachedNewsTimestamp > System.currentTimeMillis()

    suspend fun lastNews(limit: Int = 10): List<ValorantNew> = withContext(Dispatchers.IO) {
        if (!cached) {
            retrieveExperimentalValorantNews().await().take(limit)
        } else {
            cachedNews.get()!!.take(limit)
        }
    }

    private fun retrieveExperimentalValorantNews(): CompletableFuture<List<ValorantNew>> = CompletableFuture.supplyAsync {
        logger.info("Retrieving fresh Valorant news from data api...")

        val response = Unirest.get("https://playvalorant.com/page-data/en-us/news/page-data.json").asJson()

        if (!response.isSuccess) throw IllegalStateException("Non-successful status code: ${response.status}")

        val contentArray = response.body.`object`
            .getJSONObject("result")
            .getJSONObject("data")
            .getJSONObject("allContentstackArticles")
            .getJSONArray("nodes")
            .toList()
            .map { it as JSONObject }

        val mappedResults = contentArray.mapNotNull(ValorantNew::buildFromExperimentalApi)

        if (mappedResults.isNotEmpty()) {
            cachedNews.set(mappedResults)
            cachedNewsTimestamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        }

        logger.info("Successfully retrieved valorant news.")
        mappedResults
    }
}