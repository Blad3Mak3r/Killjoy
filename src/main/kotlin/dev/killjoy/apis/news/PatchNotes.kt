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

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

data class PatchNotes(
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val bannerURL: String,
    val date: Instant,
    val body: List<String>
) {

    constructor(json: JSONObject, url: String) : this(
        json.getString("id"),
        url,
        json.getString("title"),
        json.getString("description"),
        json.getJSONObject("banner").getString("url"),
        formatDate(json.getString("date")),
        json.getJSONArray("article_body").map { it as JSONObject; it.getJSONObject("rich_text_editor").getString("rich_text_editor") }
    )

    val parsedBody: String
        get() = buildString {
            for (b in body) {
                appendLine(convertHtmlToMarkdown(b))
            }
        }

    companion object {
        private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)

        private fun formatDate(date: String) = sdf.parse(date).toInstant()

        private fun convertHtmlToMarkdown(str: String): String {
            return str
                .replace("\\n".toRegex(), "")
                .replace("\\t".toRegex(), "")
                .replace("\\r".toRegex(), "")
                .replace("</?(p|div|a|ul|em)>".toRegex(), "")
                .replace("</?b>".toRegex(), "**")
                .replace("<h[1-2]>".toRegex(), "\n\n** // ")
                .replace("</h[1-2]>".toRegex(), "**\n")
                .replace("<h[3-5]>".toRegex(), "\n**")
                .replace("</h[3-5]>".toRegex(), "**\n")
                .replace("<li>".toRegex(), "\n\t- ")
                .replace("</li>".toRegex(), "")
        }
    }
}
