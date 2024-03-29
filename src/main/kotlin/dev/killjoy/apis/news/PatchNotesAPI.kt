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

import dev.killjoy.apis.news.NewsRetriever.getLocalePath
import dev.killjoy.utils.HttpUtils
import org.json.JSONObject
import java.util.*

object PatchNotesAPI {

    private const val BASE_URL = "https://playvalorant.com/page-data/%s/news"
    private const val PATCH_NOTES_URL = "${BASE_URL}/tags/patch-notes/page-data.json"

    private suspend fun getFirstArticleURL(locale: Locale): String {
        val localePath = getLocalePath(locale)
        val url = PATCH_NOTES_URL.format(localePath)

        val res = HttpUtils.await(JSONObject::class.java, url)

        if (!res.isSuccessful) throw IllegalStateException("Code: ${res.code}")
        if (res.content == null) throw IllegalStateException("Empty body.")

        val path =  res.content
            .getJSONObject("result")
            .getJSONObject("pageContext")
            .getJSONObject("data")
            .getJSONArray("articles").map { it as JSONObject }
            .firstOrNull()
            ?.getJSONObject("url")?.getString("url")
            ?: throw IllegalStateException("Articles are empty.")

        return "https://playvalorant.com/page-data/${localePath}${path}page-data.json"
    }

    private suspend fun getArticle(url: String): PatchNotes {
        val res = HttpUtils.await(JSONObject::class.java, url)

        if (!res.isSuccessful) throw IllegalStateException("Code: ${res.code}")
        if (res.content == null) throw IllegalStateException("Empty body.")

        val article = res.content
            .getJSONObject("result")
            .getJSONObject("data")
            .getJSONObject("allContentstackArticles")
            .getJSONArray("nodes")
            .map { it as JSONObject }
            .firstOrNull()
            ?: throw IllegalStateException("Article not found")

        return PatchNotes(article, url.replace("/page-data(\\.json)?".toRegex(), ""))
    }

    suspend fun latest(locale: Locale): PatchNotes {
        val url = getFirstArticleURL(locale)
        return getArticle(url)
    }
}