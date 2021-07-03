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

import dev.killjoy.i18n.I18n
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.HashMap

object NewsRetriever {
    private val cacheV2 = ConcurrentHashMap<String, I18nCachedNews>()
    private val logger = LoggerFactory.getLogger(NewsRetriever::class.java)

    fun isCached(locale: Locale): Boolean {
        val cached = cacheV2[locale.language] ?: return false

        return cached.timestamp > System.currentTimeMillis()
    }

    suspend fun lastNews(locale: Locale): List<ValorantNew> = withContext(Dispatchers.IO) {
        if (!isCached(locale)) {
            retrieveExperimentalValorantNews(locale).await()
        } else {
            cacheV2[locale.language]!!.news
        }
    }

    private fun retrieveExperimentalValorantNews(locale: Locale): CompletableFuture<List<ValorantNew>> = CompletableFuture.supplyAsync {
        logger.info("Retrieving fresh Valorant news from data api for locale ${locale.language} ...")

        val localePath = getLocalePath(locale)
        val response = Unirest.get("https://playvalorant.com/page-data/$localePath/news/page-data.json").asJson()

        if (!response.isSuccess) throw IllegalStateException("Non-successful status code: ${response.status}")

        val contentArray = response.body.`object`
            .getJSONObject("result")
            .getJSONObject("data")
            .getJSONObject("allContentstackArticles")
            .getJSONArray("nodes")
            .toList()
            .map { it as JSONObject }

        val mappedResults = contentArray.take(5).mapNotNull { ValorantNew.buildFromExperimentalApi(localePath, it) }

        if (mappedResults.isNotEmpty()) {
            val newsObj = I18nCachedNews(
                news = mappedResults,
                timestamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
            )
            cacheV2[locale.language] = newsObj
        }

        logger.info("Successfully retrieved valorant news.")
        mappedResults
    }

    private fun getLocalePath(locale: Locale): String {
        val usedLocale = if (I18n.isSupported(locale)) locale else I18n.DEFAULT_LOCALE

        return when (usedLocale.language) {
            "es" -> "es-es"
            else -> "en-us"
        }
    }
}