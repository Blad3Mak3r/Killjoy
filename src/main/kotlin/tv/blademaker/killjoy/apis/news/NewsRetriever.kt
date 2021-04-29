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

package tv.blademaker.killjoy.apis.news

import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.utils.extensions.isUrl
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object NewsRetriever {
    private var cachedNews: AtomicReference<List<ValorantNew>?> = AtomicReference(null)
    private var cachedNewsTimestamp: Long = 0L
    private val logger = LoggerFactory.getLogger(NewsRetriever::class.java)

    suspend fun lastNews(limit: Int = 10): List<ValorantNew> = coroutineScope {
        if (cachedNews.get() == null || cachedNewsTimestamp < System.currentTimeMillis()) {
            retrieveExperimentalValorantNews().await().take(limit)
        } else {
            cachedNews.get()!!.take(limit)
        }
    }

    private fun retrieveExperimentalValorantNews(): CompletableFuture<List<ValorantNew>> = CompletableFuture.supplyAsync {
        logger.info("Retrieving fresh Valorant news from experimentat data api...")

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

    data class ValorantNew(
        val url: String,
        val title: String,
        val description: String,
        val timestamp: Long,
        val image: String
    ) {

        val linkedTitle = "[$title]($url)"

        fun shortedDescription(): String {
            return if (description.length > 40) "${description.substring(0, 40)}... [Read more]($url)"
            else "$description [Read more]($url)"
        }

        companion object {
            private val dateFormat = SimpleDateFormat("MM/dd/yy")
            private val newDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)

            fun buildFromExperimentalApi(json: JSONObject): ValorantNew? {
                //

                try {
                    val title = json.getString("title")
                    val description = json.getString("description")

                    val banner = json.getJSONObject("banner").getString("url")

                    val externalLink = json.optString("external_link").takeIf { it.isNotEmpty() }
                    val url = json.getJSONObject("url").getString("url")

                    val date = json.getString("date")

                    return ValorantNew(
                        title = title,
                        url = externalLink ?: "https://playvalorant.com/en-us$url",
                        timestamp = newDateFormat.parse(date).time,
                        description = description,
                        image = banner
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            @Deprecated("Deprecated")
            fun buildFromElement(el: Element): ValorantNew? {

                try {
                    val elHref = el.attr("href")
                    val elDate = el.selectFirst("div > p[class~=NewsCard-module--published--(?i)]").text()
                    val elTitle = el.selectFirst("div > h5[class~=NewsCard-module--title--(?i)]").text()
                    val elDesc = el.selectFirst("div > p[class~=NewsCard-module--description--(?i)]").text()

                    val elImageSpan = el.selectFirst("div > span[class~=NewsCard-module--image--(?i)]")
                        .attr("style")

                    val elImageUrl = elImageSpan.substring(elImageSpan.indexOf("https://"), elImageSpan.indexOf(")"))

                    return ValorantNew(
                        title = elTitle,
                        url = if (elHref.isUrl()) elHref else "https://playvalorant.com${elHref}",
                        timestamp = dateFormat.parse(elDate).time,
                        description = elDesc,
                        image = elImageUrl
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }

        override fun toString(): String {
            return "ValorantNew(url='$url', title='$title', description='$description', timestamp=$timestamp, image='$image')"
        }
    }
}