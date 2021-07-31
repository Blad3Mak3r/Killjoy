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

import net.dv8tion.jda.api.entities.MessageEmbed
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class PatchNotes(
    val id: String,
    val uid: String,
    val title: String,
    val description: String,
    val bannerURL: String,
    val date: String,
    val body: List<String>
) {

    constructor(json: JSONObject) : this(
        json.getString("id"),
        json.getString("uid"),
        json.getString("title"),
        json.getString("description"),
        json.getJSONObject("banner").getString("url"),
        formatDate(json.getString("date")),
        json.getJSONArray("article_body").map { it as JSONObject; it.getJSONObject("rich_text_editor").getString("rich_text_editor") }
    )

    fun parsed(): List<String> {
        val list = mutableListOf<String>()

        for (line in body) {
            list.add(parseLine(line))
        }

        return list
    }

    private fun parseLine(line: String): String {
        return line.replace("</?p>".toRegex(), "")
    }

    val parsedBody: String
        get() = buildString {
            for (b in body) {
                appendLine(convertHtmlToMarkdown(b))
            }
        }

    companion object {
        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        private val outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)

        private fun formatDate(date: String): String {
            val formatted = LocalDate.parse(date, inputFormatter)
            return outputFormatter.format(formatted)
        }

        private fun convertHtmlToMarkdown(str: String): String {
            return str.replace("</?(p|div|a|ul|em)>".toRegex(), "")
                .replace("</?b>".toRegex(), "**")
                .replace("<h[1-2]>".toRegex(), "\n** // ")
                .replace("<h[3-5]>".toRegex(), "**")
                .replace("</h[1-5]>".toRegex(), "**")
                .replace("<li>".toRegex(), "\n- ")
                .replace("</li>".toRegex(), "")
        }

        private val ESCAPES = mapOf(
            Regex("</?h2>") to "**",
            Regex("</?p>") to "",
            Regex("</?div>") to ""
        )
    }
}
