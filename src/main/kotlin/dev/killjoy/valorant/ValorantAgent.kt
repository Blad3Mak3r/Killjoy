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

package dev.killjoy.valorant

import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.i18n.I18n
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.utils.extensions.isUrl
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
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
        Skill.ofAll(json.getJSONArray("skills"))
    )

    init {
        check(this.avatar.isUrl()) { "avatar is not a valid url (ValorantAgent ${this.name}) [${this.avatar}]" }
    }

    suspend fun asEmbed(guild: Guild): EmbedBuilder {
        val stats = RiotAPI.AgentStatsAPI.getAgentStatsAsync(name.lowercase()).await()

        val statistics = if (stats == null) guild.i18n(I18nKey.NOT_AVAILABLE_AT_THE_MOMENT)
        else guild.i18nCommand("agent.stats", stats.pickRate, stats.winRate, stats.kdaPerMatch, stats.kdaPerRound, stats.avgDamage, stats.avgScore)

        return EmbedBuilder().apply {
            setAuthor(role.locatedName(guild), null, role.iconUrl)
            setTitle(name, "https://playvalorant.com/en-us/agents/${name.replace("/", "-").lowercase()}/")
            setThumbnail(avatar)
            setDescription(bio)
            addField(guild.i18nCommand("agent.origin"), origin, true)
            addField(guild.i18nCommand("agent.gender"), gender, true)
            addField(guild.i18nCommand("agent.affiliation"), affiliation, true)
            addField(guild.i18nCommand("agent.statistics"), statistics, false)
            addBlankField(false)
            setDefaultColor()
            for (skill in skills) {
                addField(skill.asEmbedField())
            }
        }
    }

    enum class Role(val emoji: String, val iconUrl: String, private val i18nKey: I18nKey) {
        Controller("<:controller:754676227809214485>", "https://i.imgur.com/V4Ci1Oh.png", I18nKey.AGENT_CLASS_CONTROLLER),
        Duelist("<:duelist:754676227952083025>", "https://i.imgur.com/rs0d2qx.png", I18nKey.AGENT_CLASS_DUELIST),
        Initiator("<:initiator:754676227582722062>", "https://i.imgur.com/hCVcqgf.png", I18nKey.AGENT_CLASS_INITIATOR),
        Sentinel("<:sentinel:754676227994026044>", "https://i.imgur.com/ODX86kl.png", I18nKey.AGENT_CLASS_SENTINEL);

        fun locatedName(guild: Guild): String = I18n.getTranslate(guild, this.i18nKey)

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

        fun asEmbedField() = MessageEmbed.Field(
            name,
            "$info\n` Cost: $cost `",
            false
        )

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
                return str.trim().lowercase().replace(" ", "").replace("’", "").replace("'", "").replace("\"", "")
            }
        }
    }
}
