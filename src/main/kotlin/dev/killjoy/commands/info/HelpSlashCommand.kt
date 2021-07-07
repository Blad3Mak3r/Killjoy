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

package dev.killjoy.commands.info

import dev.killjoy.*
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.i18n.I18n
import dev.killjoy.i18n.i18nCommand
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button

class HelpSlashCommand : AbstractSlashCommand("help") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.replyEmbed {
            setTitle(ctx.i18nCommand("help.title"))
            setDescription(ctx.i18nCommand("help.description"))
            addBlankField(false)
            addField(ctx.i18nCommand("help.fields.0.title"), ctx.i18nCommand("help.fields.0.content"), false)
            addBlankField(false)
            addField(ctx.i18nCommand("help.fields.1.title"), guildLocale(ctx.guild), true)
            addField(ctx.i18nCommand("help.fields.2.title"), killjoyLocale(ctx.guild), true)
        }.addActionRows(buildActionRows(ctx.guild)).queue()
    }

    companion object {
        private fun guildLocale(guild: Guild): String {
            val isSupported = I18n.isSupported(guild.locale)
            return guild.locale.getDisplayLanguage(guild.locale).let {
                if (!isSupported) it.plus(" (Not Supported)") else it
            }.capitalize(guild.locale)
        }

        private fun killjoyLocale(guild: Guild): String {
            return guild.supportedLocale.getDisplayLanguage(guild.locale).capitalize(guild.locale)
        }

        fun buildActionRows(guild: Guild) = ActionRow.of(
            Button.link(INVITE_URL, guild.i18nCommand("help.links.invite")),
            Button.link(WEBSITE_URL, guild.i18nCommand("help.links.website")),
            Button.link("$WEBSITE_URL/commands", guild.i18nCommand("help.links.commands")),
            Button.link(GUIDES_URL, guild.i18nCommand("help.links.guides")),
            Button.link(VOTE_URL, guild.i18nCommand("help.links.vote"))
        )
    }
}