/*******************************************************************************
 * Copyright (c) 2020. Blademaker
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

package net.hugebot.memes4k

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import org.slf4j.LoggerFactory
import kotlin.random.Random

object Memes4K {

    private val httpClient: HttpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    private suspend fun doRequest(subreddit: String) = httpClient.get<HttpResponse>(getBaseUrl(subreddit))

    @Throws(IllegalStateException::class)
    suspend fun getMeme(subreddit: String): Meme? {
        return try {
            val r = doRequest(subreddit)
            val json = JSONObject(r.readText())
            Meme.buildFromJson(json)
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }

    suspend fun getMemeAsync(subreddit: String) = coroutineScope { async { getMeme(subreddit) } }

    const val VERSION = "0.1.0"
    private val DEFAULT_REDDITS = listOf("memes", "dankmemes", "meirl")
    private const val BASE_URL = "https://www.reddit.com/r/%s/hot/.json?count=100"

    private fun getBaseUrl(subreddit: String) = String.format(BASE_URL, subreddit)
    private fun getRandomReddit() = DEFAULT_REDDITS[Random.nextInt(DEFAULT_REDDITS.size)]

    private val logger = LoggerFactory.getLogger(Memes4K::class.java)
}