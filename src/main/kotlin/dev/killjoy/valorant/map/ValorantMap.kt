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

package dev.killjoy.valorant.map

import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.valorant.I18nMap
import dev.killjoy.valorant.ValorantEntity
import dev.killjoy.valorant.buildI18nMap
import net.dv8tion.jda.api.entities.Guild
import org.json.JSONObject

@Suppress("unused")
data class ValorantMap(
    override val name: String,
    private val description: I18nMap,
    val minimap: String,
    val imageUrl: String
) : ValorantEntity {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.getString("name"),
        buildI18nMap(jsonObject.getJSONObject("description")),
        jsonObject.getString("minimap"),
        jsonObject.getString("image_url")
    )

    fun description(guild: Guild) = description[guild.supportedLocale.language]!!
}