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

data class PatchNotes(
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
        json.getString("date"),
        json.getJSONArray("article_body").map { it as JSONObject; it.getJSONObject("rich_text_editor").getString("rich_text_editor") }
    )

    val parsedBody: String
        get() = buildString {
            for (b in body) {
                appendLine(b)
            }
        }
}
