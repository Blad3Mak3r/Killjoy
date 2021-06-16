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

import kong.unirest.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class ValorantNew(
    val url: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val image: String
) {

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)

        fun buildFromExperimentalApi(json: JSONObject): ValorantNew? {
            //

            try {
                val title = json.getString("title")
                val description = json.getString("description")

                val banner = json.getJSONObject("banner").getString("url")

                val externalLink = json.optString("external_link").takeIf { it.isNotEmpty() }
                val url = json.getJSONObject("url").getString("url")

                val date = json.getString("date")

                return ValorantNew(
                    title = title,
                    url = externalLink ?: "https://playvalorant.com/en-us$url",
                    timestamp = dateFormat.parse(date).time,
                    description = description,
                    image = banner
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