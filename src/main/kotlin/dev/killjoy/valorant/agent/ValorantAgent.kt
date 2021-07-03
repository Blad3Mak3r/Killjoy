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

package dev.killjoy.valorant.agent

import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.i18n.I18n
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.utils.extensions.isUrl
import dev.killjoy.valorant.ValorantEntity
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
class ValorantAgent(json: JSONObject) : ValorantEntity {

    val id: String? = json.getString("id").takeIf { it.isNotEmpty() }
    val number: Int = json.getInt("number")
    private val apiName: String? = json.optString("api_name").trim().takeIf { it.isNotEmpty() }
    override val name: String = json.getString("name").trim()
    private val bio: Map<String, String> = buildI18nMap(json.getJSONObject("bio"))
    private val gender: AgentGender = AgentGender.of(json.getString("gender"))
    val affiliation: String = json.getString("affiliation")
    val origin: String = json.getString("origin").trim()
    val role: AgentRole = AgentRole.of(json.getString("role"))
    val avatar: String = json.getString("avatar")
    val abilities: List<AgentAbility> = AgentAbility.ofAll(this, json.getJSONArray("skills"))

    init {
        check(this.avatar.isUrl()) { "avatar is not a valid url (ValorantAgent ${this.name}) [${this.avatar}]" }
    }

    fun bio(guild: Guild): String {
        return bio[guild.supportedLocale.language] ?: "No translates available."
    }

    fun gender(guild: Guild): String {
        return guild.i18n(gender.i18nKey)
    }

    suspend fun asEmbed(guild: Guild): EmbedBuilder {
        val stats = RiotAPI.AgentStatsAPI.getAgentStatsAsync(name.lowercase()).await()

        val statistics = if (stats == null) guild.i18n(I18nKey.NOT_AVAILABLE_AT_THE_MOMENT)
        else guild.i18nCommand("agent.stats", stats.pickRate, stats.winRate, stats.kdaPerMatch, stats.kdaPerRound, stats.avgDamage, stats.avgScore)

        return EmbedBuilder().apply {
            setAuthor(role.locatedName(guild), null, role.iconUrl)
            setTitle(name, "https://playvalorant.com/en-us/agents/${name.replace("/", "-").lowercase()}/")
            setThumbnail(avatar)
            setDescription(bio(guild))
            addField(guild.i18nCommand("agent.origin"), origin, true)
            addField(guild.i18nCommand("agent.gender"), gender(guild), true)
            addField(guild.i18nCommand("agent.affiliation"), affiliation, true)
            addField(guild.i18nCommand("agent.statistics"), statistics, false)
            addBlankField(false)
            setDefaultColor()
            for (ability in abilities) {
                addField(ability.asEmbedField(guild))
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

    companion object {
        fun buildI18nMap(json: JSONObject): Map<String, String> {
            val names = I18n.VALID_LOCALES.map { it.language }
            val map = HashMap<String, String>()

            for (name in names) {
                map[name] = json.getString(name)
            }

            return map
        }
    }
}
