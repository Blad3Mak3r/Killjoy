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

import dev.killjoy.Launcher
import dev.killjoy.apis.news.NewsRetriever.getLocalePath
import kotlinx.coroutines.future.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture

object PatchNotesAPI {

    private const val BASE_URL = "https://playvalorant.com/page-data/%s/news"
    private const val PATCH_NOTES_URL = "${BASE_URL}/tags/patch-notes/page-data.json"

    private fun getFirstArticleURL(locale: Locale): CompletableFuture<String> {
        val future = CompletableFuture<String>()

        val localePath = getLocalePath(locale)
        val url = PATCH_NOTES_URL.format(localePath)

        val request = Request.Builder().run {
            url(url)
            build()
        }

        Launcher.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { r ->
                    if (!r.isSuccessful) {
                        future.completeExceptionally(IllegalStateException("Received non-successful status code."))
                        return
                    }

                    val body = r.body?.string()
                    if (body == null) {
                        future.completeExceptionally(IllegalStateException("Body is empty."))
                        return
                    }

                    val content = JSONObject(body)

                    val articles = content
                        .getJSONObject("results")
                        .getJSONObject("pageContext")
                        .getJSONObject("data")
                        .getJSONArray("articles").map { it as JSONObject }

                    if (articles.isEmpty()) {
                        future.completeExceptionally(IllegalStateException("Articles are empty."))
                        return
                    }

                    val first = articles.first()

                    future.complete(first.getJSONObject("url").getString("url"))
                }
            }
        })

        return future
    }

    private fun getArticle(url: String): CompletableFuture<PatchNotes> {
        val future = CompletableFuture<PatchNotes>()

        val req = Request.Builder().run {
            build()
        }

        Launcher.httpClient.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { r ->
                    if (!r.isSuccessful) {
                        future.completeExceptionally(IllegalStateException("Received non-successful status code."))
                        return
                    }

                    val body = r.body?.string()

                    if (body == null) {
                        future.completeExceptionally(IllegalStateException("Received empty body."))
                        return
                    }

                    val content = JSONObject(body)

                    val article = PatchNotes()
                }
            }

        })


        return future
    }

    suspend fun latest(locale: Locale): PatchNotes {
        val url = getFirstArticleURL(locale).await()

        return getArticle(url).await()
    }
}