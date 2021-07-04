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
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandArgument
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.i18n.i18nCommand

@CommandProperties(
    name = "maps",
    category = Category.Game,
    aliases = ["map"],
    arguments = [
        CommandArgument("name", "Map name", false)
    ])
class MapsCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        val mapsUrl = "https://playvalorant.com/${NewsRetriever.getLocalePath(ctx.guild.supportedLocale)}/maps/"

        if (ctx.args.isEmpty()) {
            ctx.replyEmbed {
                setTitle(ctx.guild.i18nCommand("maps.header"), mapsUrl)
                for (map in Launcher.maps) {
                    addField(map.name.capitalize(), map.description(ctx.guild), false)
                }
            }.queue()
        } else {
            val map = Launcher.getMap(ctx.args.first())
                ?: return ctx.reply(ctx.guild.i18nCommand("maps.notFound", ctx.args.first())).queue()

            ctx.replyEmbed {
                setTitle(map.name.capitalize(), mapsUrl)
                setDescription(map.description(ctx.guild))
                setThumbnail(map.minimap)
                setImage(map.imageUrl)
            }.queue()
        }
    }

    override val help: String = "Get a list of maps or information about a specific map from Valorant"

}