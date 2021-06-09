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

import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.bot.framework.ColorExtra
import dev.killjoy.bot.utils.extensions.isUrl
import net.dv8tion.jda.api.EmbedBuilder
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
data class ValorantAgent (
    val id: String? = null,
    val number: Int,
    private val apiName: String? = null,
    override val name: String,
    val bio: String,
    val gender: String,
    val affiliation: String,
    val origin: String,
    val role: Role,
    val avatar: String,
    val thumbnail: String,
    val skills: List<Skill>
) : ValorantEntity {

    val abilities = skills.map { AgentAbility(this, it) }

    constructor(json: JSONObject) : this(
        json.getString("id").takeIf { it.isNotEmpty() },
        json.getInt("number"),
        json.optString("api_name").trim().takeIf { it.isNotEmpty() },
        json.getString("name").trim(),
        json.getString("bio").trim(),
        json.getString("gender"),
        json.getString("affiliation"),
        json.getString("origin").trim(),
        Role.of(json.getString("role")),
        json.getString("avatar"),
        json.getString("thumbnail"),
        Skill.ofAll(json.getJSONArray("skills"))
    )

    init {
        check(this.avatar.isUrl()) { "avatar is not a valid url (ValorantAgent ${this.name}) [${this.avatar}]" }
    }

    suspend fun asEmbed(): EmbedBuilder {
        val stats = RiotAPI.AgentStatsAPI.getAgentStatsAsync(name.toLowerCase()).await()

        val statistics = buildString {
            appendLine("__**Pick Rate:**__     ${stats?.pickRate ?: "N/A"}")
            appendLine("__**Win Rate:**__      ${stats?.winRate ?: "N/A"}")
            appendLine("__**KDA (match):**__   ${stats?.kdaPerMatch ?: "N/A"}")
            appendLine("__**KDA (round):**__   ${stats?.kdaPerRound ?: "N/A"}")
            appendLine("__**AVG. Damage:**__   ${stats?.avgDamage ?: "N/A"}")
            appendLine("__**AVG. Score:**__    ${stats?.avgScore ?: "N/A"}")
        }
        val info = buildString {
            appendLine("__**Origin:**__      $origin")
            appendLine("__**Gender:**__      $gender")
            appendLine("__**Affiliation:**__ $affiliation")
        }

        return EmbedBuilder().apply {
            setAuthor(role.name, null, role.iconUrl)
            setTitle(name, "https://playvalorant.com/en-us/agents/${name.toLowerCase()}/")
            setThumbnail(avatar)
            //setImage(thumbnail)
            setDescription(bio)
            addField("Information", info, true)
            addField("Statistics", statistics, true)
            addBlankField(false)
            setColor(ColorExtra.VAL_RED)
            for (skill in skills) {
                addField("`` ${skill.button.name.toUpperCase()} `` - ${skill.name}", skill.info, false)
            }
        }
    }

    enum class Role(val emoji: String, val iconUrl: String) {
        Controller("<:controller:754676227809214485>", "https://i.imgur.com/V4Ci1Oh.png"),
        Duelist("<:duelist:754676227952083025>", "https://i.imgur.com/rs0d2qx.png"),
        Initiator("<:initiator:754676227582722062>", "https://i.imgur.com/hCVcqgf.png"),
        Sentinel("<:sentinel:754676227994026044>", "https://i.imgur.com/ODX86kl.png");

        val snowFlake: String
            get() = emoji.removePrefix("<").removeSuffix(">")

        companion object {
            fun of(str: String): Role {
                return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid role name.")
            }
        }
    }

    data class Skill(
        val button: Button,
        val name: String,
        val iconUrl: String,
        val info: String,
        val preview: String,
        val cost: String = ""
    ) {

        constructor(json: JSONObject) : this(
            Button.of(json.getString("button")),
            json.getString("name").trim(),
            json.getString("iconUrl").trim(),
            json.getString("info").trim(),
            json.getString("preview").trim(),
            json.optString("cost", "")
        )

        init {
            check(this.iconUrl.isUrl()) { "iconUrl is not a valid URL. (Skill $name) [${this.iconUrl}]" }
            check(this.preview.isUrl()) { "preview is not a valid URL. (Skill $name) [${this.preview}]" }
        }

        val id: String
            get() = buildIdentifier(name)

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
            fun ofAll(array: JSONArray) = array.map { it as JSONObject }.map { Skill(it) }

            private fun buildIdentifier(str: String): String {
                return str.trim().toLowerCase().replace(" ", "").replace("’", "").replace("'", "").replace("\"", "")
            }
        }
    }
}
