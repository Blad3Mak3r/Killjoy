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

package dev.killjoy.apis.memes

import dev.killjoy.utils.HttpUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import kotlin.random.Random

object RedditMemes {

    @Throws(IllegalStateException::class)
    suspend fun get(subreddit: String): Meme? {
        return try {
            val json = doRequest(subreddit)
            Meme.buildFromJson(json)
        } catch (e: Throwable) {
            logger.error(e.message, e)
            null
        }
    }

    suspend fun retrieveValidMemes(subreddit: String): List<Meme> {
        val json = doRequest(subreddit).getJSONObject("data").getJSONArray("children")

        val validMemes = json.map {
            it as JSONObject; it.getJSONObject("data")
        }.filter {
            it.has("url") && Meme.hasImage(it.getString("url"))
        }.map {
            Meme(it)
        }

        if (validMemes.isEmpty()) error("Memes not received...")

        return validMemes
    }

    fun select(memes: List<Meme>): Meme {
        if (memes.isEmpty()) error("Meme list is empty.")

        return memes[Random.nextInt(memes.size)]
    }

    private suspend fun doRequest(subreddit: String): JSONObject {
        val r = HttpUtils.await(JSONObject::class.java) {
            url(baseURL(subreddit))
        }
        val json = r.content

        check(r.isSuccessful) { "Non-successful status code ${r.code}" }
        check(json != null) { "Received empty body." }

        return json
    }

    private const val BASE_URL = "https://www.reddit.com/r/%s/hot/.json?count=100"

    private fun baseURL(subreddit: String) = String.format(BASE_URL, subreddit)

    private val logger = LoggerFactory.getLogger(RedditMemes::class.java)
}