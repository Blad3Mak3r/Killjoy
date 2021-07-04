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

import dev.killjoy.Launcher
import dev.killjoy.apis.news.NewsRetriever
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext

@Suppress("unused")
class MapsSlashCommand : AbstractSlashCommand("maps") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.event.deferReply().queue()

        val selection = ctx.getOption("map")?.asString

        val mapsUrl = "https://playvalorant.com/${NewsRetriever.getLocalePath(ctx.guild.supportedLocale)}/maps/"

        if (selection == null) {
            ctx.sendEmbed {
                setTitle(ctx.i18nCommand("maps.header"), mapsUrl)
                for (map in Launcher.maps) {
                    addField(map.name.capitalize(), map.description(ctx.guild), false)
                }
            }.queue()
        } else {
            val map = Launcher.getMap(selection)
                ?: return ctx.sendNotFound(ctx.i18nCommand("maps.notFound", selection)).queue()

            ctx.sendEmbed {
                setTitle(map.name.capitalize(), mapsUrl)
                setDescription(map.description(ctx.guild))
                setThumbnail(map.minimap)
                setImage(map.imageUrl)
            }.queue()
        }
    }

}