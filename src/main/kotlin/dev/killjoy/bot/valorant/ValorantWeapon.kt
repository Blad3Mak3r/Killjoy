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

@file:Suppress("unused")

package dev.killjoy.bot.valorant

import dev.killjoy.bot.framework.ColorExtra
import net.dv8tion.jda.api.EmbedBuilder
import org.json.JSONObject

@Suppress("unused")
data class ValorantWeapon(
    override val name: String,
    val short: String,
    val type: Type,
    val descriptions: List<String>,
    val thumbnail: String,
    val cost: Int,
    val magazine: Int?,
    val wallPenetration: String?
) : ValorantEntity {

    val id: String
        get() = name.toLowerCase().replace(" ", "").replace("-", "")

    constructor(json: JSONObject) : this(
            json.getString("name"),
            json.getString("short"),
            Type.of(json.getString("type")),
            json.getJSONArray("descriptions").map { it as String },
            json.getString("thumbnail"),
            json.getInt("cost"),
            kotlin.runCatching { json.getInt("magazine") }.getOrNull(),
            kotlin.runCatching { json.getString("wall_penetration") }.getOrNull()
    )

    fun asEmbed(): EmbedBuilder {
        return EmbedBuilder().apply {
            setAuthor(type.name.toUpperCase())
            setTitle(name)
            setColor(ColorExtra.VAL_RED)
            setDescription(short)
            addField("Cost", "<:creds:755356472132501574> $cost", true)
            if (magazine != null) addField("Magazine", "$magazine", true)
            if (wallPenetration != null) addField("Wall Penetration", wallPenetration, true)
            addField("Info", descriptions.joinToString("\n") { " • $it" }, false)
            setImage(thumbnail)
        }
    }

    enum class Type {
        Smgs,
        Rifles,
        Shotguns,
        Snipers,
        Melee,
        Heavies,
        Sidearms;

        companion object {
            fun of(str: String): Type {
                return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid type name.")
            }
        }
    }
}