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
import dev.killjoy.utils.HttpUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*

object NewsRetriever {
    private val logger = LoggerFactory.getLogger(NewsRetriever::class.java)

    suspend fun retrieveNews(locale: Locale): List<ValorantNew> {
        logger.info("Retrieving fresh Valorant news from data api for locale ${locale.language} ...")

        val localePath = getLocalePath(locale)
        val response = HttpUtils.await(JSONObject::class.java) {
            url("https://playvalorant.com/page-data/$localePath/news/page-data.json")
        }

        val content = response.content

        check(content != null) { "Received empty body." }
        check(response.isSuccessful) { "Non-successful status code: ${response.code}" }

        val contentArray = content
            .getJSONObject("result")
            .getJSONObject("data")
            .getJSONObject("allContentstackArticles")
            .getJSONArray("nodes")
            .map { it as JSONObject }

        val mappedResults = contentArray.take(5).mapNotNull { ValorantNew.buildFromExperimentalApi(localePath, it) }

        logger.info("Successfully retrieved valorant news.")

        return mappedResults
    }

    fun getLocalePath(locale: Locale): String {
        val usedLocale = if (I18n.isSupported(locale)) locale else I18n.DEFAULT_LOCALE

        return when (usedLocale.language) {
            "es" -> "es-es"
            else -> "en-us"
        }
    }
}