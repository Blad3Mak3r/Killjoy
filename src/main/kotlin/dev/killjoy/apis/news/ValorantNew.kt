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

import dev.killjoy.utils.ParseUtils
import kong.unirest.json.JSONObject
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

data class ValorantNew(
    val url: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val image: String
) {

    fun asEmbedField(): MessageEmbed.Field {
        val date = ParseUtils.millisToCalendar(timestamp)
        return MessageEmbed.Field(
            title,
            fieldPattern.format(description, url, date),
            false
        )
    }

    companion object {
        private const val fieldPattern = "%s\n[` Read more... `](%s) | ` Posted on %s `"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        private val logger = LoggerFactory.getLogger(ValorantNew::class.java)

        fun buildFromExperimentalApi(json: JSONObject): ValorantNew? {
            try {
                val title = json.getString("title")
                val description = json.getString("description")

                val banner = json.getJSONObject("banner").getString("url")

                val externalLink = json.optString("external_link").takeIf { it.isNotEmpty() }
                val url = json.getJSONObject("url").getString("url")

                val date = json.getString("date")

                val new = ValorantNew(
                    title = title,
                    url = externalLink ?: "https://playvalorant.com/en-us$url",
                    timestamp = dateFormat.parse(date).time,
                    description = description,
                    image = banner
                )

                return new
            } catch (e: Exception) {
                logger.error("Error creating Valorant New: ${e.message}", e)
                return null
            }
        }
    }
}