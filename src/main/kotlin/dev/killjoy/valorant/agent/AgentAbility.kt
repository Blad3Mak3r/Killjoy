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

package dev.killjoy.valorant.agent

import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.json.JSONArray
import org.json.JSONObject

data class AgentAbility(
    val agent: ValorantAgent,
    val button: Button,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val iconUrl: String,
    val preview: String,
    val cost: String
) {

    constructor(agent: ValorantAgent, json: JSONObject) : this(
        agent,
        Button.of(json.getString("button")),
        buildMap(json.getJSONObject("name")),
        buildMap(json.getJSONObject("info")),
        json.getString("iconUrl"),
        json.getString("preview"),
        json.getString("cost")
    )

    fun asEmbedField(guild: Guild): MessageEmbed.Field {
        val lang = guild.supportedLocale.language

        return MessageEmbed.Field(
            name[lang],
            "${description[lang]}\n` ${guild.i18n(I18nKey.ABILITY_COST)}: $cost `",
            false
        )
    }

    fun asEmbed(guild: Guild): MessageEmbed {
        val lang = guild.supportedLocale.language

        return EmbedBuilder().run {
            setAuthor(agent.name, null, agent.avatar)
            setTitle(name[lang])
            setDescription(description[lang])
            setThumbnail(iconUrl)
            setImage(preview)
            addField("Action Button", "`${button.name}`", true)
            addField("Usage Cost", "`${cost}`", true)
            setDefaultColor()
            build()
        }
    }

    enum class Button {
        Q,
        E,
        C,
        X;

        companion object {
            fun of(str: String): Button {
                return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid role name.")
            }
        }
    }

    companion object {
        fun ofAll(agent: ValorantAgent, array: JSONArray) = array.map { it as JSONObject }.map { AgentAbility(agent, it) }

        fun buildMap(json: JSONObject): Map<String, String> {
            val names = json.names().map { "$it" }
            val map = HashMap<String, String>()

            for (name in names) {
                map[name] = json.getString(name)
            }

            return map
        }
    }
}