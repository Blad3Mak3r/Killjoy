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

import dev.killjoy.i18n.i18nCommand
import dev.killjoy.utils.ParseUtils
import kong.unirest.json.JSONObject
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class ValorantNew(
    val url: String,
    val title: String,
    val description: String,
    val date: String,
    val image: String
) {

    fun asEmbedField(guild: Guild): MessageEmbed.Field {
        return MessageEmbed.Field(
            title,
            MessageFormat.format(getFieldPattern(guild), description, url, date),
            false
        )
    }

    companion object {
        private fun getFieldPattern(guild: Guild) = guild.i18nCommand("news.fieldPattern")
        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        private val outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
        private val logger = LoggerFactory.getLogger(ValorantNew::class.java)

        private fun formatDate(date: String): String {
            val formatted = LocalDate.parse(date, inputFormatter)
            return outputFormatter.format(formatted)
        }

        fun buildFromExperimentalApi(localePath: String, json: JSONObject): ValorantNew? {
            try {
                val title = json.getString("title")
                val description = json.getString("description")

                val banner = json.getJSONObject("banner").getString("url")

                val externalLink = json.optString("external_link").takeIf { it.isNotEmpty() }
                val url = json.getJSONObject("url").getString("url")

                return ValorantNew(
                    title = title,
                    url = externalLink ?: "https://playvalorant.com/$localePath$url",
                    date = formatDate(json.getString("date")),
                    description = description,
                    image = banner
                )
            } catch (e: Exception) {
                logger.error("Error creating Valorant New: ${e.message}", e)
                return null
            }
        }
    }
}