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

package dev.killjoy.valorant.arsenal

import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.valorant.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import org.json.JSONObject

@Suppress("unused")
data class ValorantWeapon(
    override val name: String,
    val locatedNames: I18nMap? = null,
    val short: I18nMap,
    private val type: WeaponType,
    val descriptions: Map<String, List<String>>,
    val thumbnail: String,
    val cost: Int,
    val magazine: Int?,
    private val wallPenetration: WallPenetration?
) : ValorantEntity {
    val ids = buildWeaponIDs(this)
    val names = buildWeaponNames(this)

    constructor(json: JSONObject) : this(
        json.getString("name"),
        buildNullableI18nMap(json,"located_names"),
        buildI18nMap(json.getJSONObject("short")),
        WeaponType.of(json.getString("type")),
        buildWeaponDescriptions(json.getJSONObject("descriptions")),
        json.getString("thumbnail"),
        json.getInt("cost"),
        kotlin.runCatching { json.getInt("magazine") }.getOrNull(),
        WallPenetration.of(json)
    )

    fun name(guild: Guild): String {
        return if (locatedNames == null) name
        else locatedNames[guild.supportedLocale.language]!!
    }

    fun short(guild: Guild) = short[guild.supportedLocale.language]!!

    fun type(guild: Guild) = guild.i18n(type.i18nKey)

    private fun descriptions(guild: Guild) = descriptions[guild.supportedLocale.language]!!

    private fun wallPenetration(guild: Guild) = wallPenetration?.let { guild.i18n(it.i18nKey) }

    fun asEmbed(guild: Guild): EmbedBuilder {
        return EmbedBuilder().apply {
            setAuthor(type(guild))
            setTitle(name(guild))
            setDefaultColor()
            setDescription(short(guild))
            addField(guild.i18n(I18nKey.ABILITY_COST), "<:creds:755356472132501574> $cost", true)
            if (magazine != null) addField(guild.i18nCommand("arsenal.magazine"), "$magazine", true)
            if (wallPenetration != null) addField(guild.i18nCommand("arsenal.penetration"), wallPenetration(guild), true)
            addField("Info", descriptions(guild).joinToString("\n") { " • $it" }, false)
            setImage(thumbnail)
        }
    }
}