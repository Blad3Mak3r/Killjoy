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
import io.ktor.util.*
import org.json.JSONObject
import org.slf4j.LoggerFactory

object RedditMemes {

    @Throws(IllegalStateException::class)
    suspend fun get(subreddit: String): Meme? {
        return try {
            val r = HttpUtils.await(JSONObject::class.java) {
                url(baseURL(subreddit))
            }
            val json = r.content

            check(r.isSuccessful) { "Non-successful status code ${r.code}" }
            check(json != null) { "Received empty body." }
            Meme.buildFromJson(json)
        } catch (e: Throwable) {
            logger.error(e)
            null
        }
    }

    private const val BASE_URL = "https://www.reddit.com/r/%s/hot/.json?count=100"

    private fun baseURL(subreddit: String) = String.format(BASE_URL, subreddit)

    private val logger = LoggerFactory.getLogger(RedditMemes::class.java)
}