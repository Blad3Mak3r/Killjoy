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

package dev.killjoy.commands.game

import dev.killjoy.apis.news.PatchNotesAPI
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.i18n.i18nCommand
import tv.blademaker.slash.api.BaseSlashCommand
import tv.blademaker.slash.api.SlashCommandContext

@Suppress("unused")
class PatchNotesSlashCommand : BaseSlashCommand("patchnotes") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        val latest = PatchNotesAPI.latest(ctx.guild.supportedLocale)

        val fullDescription = buildString {
            appendLine(latest.description)
            appendLine(latest.parsedBody)
        }.let {
            if (it.length > 4096) {
                it.substring(0, 3900).plus("...\n**[READ MORE](${latest.url})**")
            } else it
        }

        ctx.sendEmbed {
            setTitle(latest.title)
            setDescription(fullDescription)
            setImage(latest.bannerURL)
            setFooter(ctx.i18nCommand("patchnotes.publishedAt"))
            setTimestamp(latest.date)
        }.queue()
    }

}