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

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.utils.extensions.isUrl
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference

object NewsRetriever {
    private var cachedNews: AtomicReference<List<ValorantNew>?> = AtomicReference(null)
    private var cachedNewsTimestamp: Long = 0L
    private val logger = LoggerFactory.getLogger(NewsRetriever::class.java)

    suspend fun lastNews(limit: Int = 10): List<ValorantNew> = coroutineScope {
        if (cachedNews.get() == null || cachedNewsTimestamp < System.currentTimeMillis()) {
            retrieveValorantNews().await().take(limit)
        } else {
            cachedNews.get()!!.take(limit)
        }
    }

    private fun retrieveValorantNews(): CompletableFuture<List<ValorantNew>> {
        val future = CompletableFuture<List<ValorantNew>>()
        logger.info("Retrieving new valorant news...")
        
        try {
            val document = Jsoup.connect("https://playvalorant.com/en-us/news/")
                .get()

            //val featuredNews = document.select("div.news-card > a")
            val latestNews = document.select("div.NewsArchive-module--content--_kqJU > div > div > a")
            val mappedResults = latestNews.mapNotNull(ValorantNew::buildFromElement)

            if (mappedResults.isNotEmpty()) {
                cachedNews.set(mappedResults)
                cachedNewsTimestamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
            }

            logger.info("Successfully retrieved valorant news.")
            future.complete(mappedResults)
        } catch (e: Exception) {
            logger.error("Error retrieving valorant news...", e)
            future.completeExceptionally(e)
        }

        return future
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

            fun buildFromElement(el: Element): ValorantNew? {

                try {
                    val elHref = el.attr("href")
                    val elDate = el.selectFirst("div > p.NewsCard-module--published--37jmR").text()
                    val elTitle = el.selectFirst("div > h5.NewsCard-module--title--1MoLu").text()
                    val elDesc = el.selectFirst("div > p.NewsCard-module--description--3sFiD").text()

                    val elImageSpan = el.selectFirst("div > span.NewsCard-module--image--2sGrc")
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