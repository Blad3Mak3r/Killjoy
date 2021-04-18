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

package tv.blademaker.killjoy.commands.game

import tv.blademaker.killjoy.Launcher
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext

@Suppress("unused")
class MapsSlashCommand : AbstractSlashCommand("maps") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.event.acknowledge().queue()

        val selection = ctx.getOption("map")?.asString

        if (selection == null) {
            ctx.sendEmbed {
                setTitle("Valorant maps", "https://playvalorant.com/en-us/maps/")
                for (map in Launcher.maps) {
                    addField(map.name.capitalize(), map.description, false)
                }
            }.queue()
        } else {
            val map = Launcher.getMap(selection)
                ?: return ctx.sendNotFound("There is no map called `` $selection ``.").queue()

            ctx.sendEmbed {
                setTitle(map.name.capitalize(), "https://playvalorant.com/en-us/maps/")
                setDescription(map.description)
                setThumbnail(map.minimap)
                setImage(map.imageUrl)
            }.queue()
        }
    }

}