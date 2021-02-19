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
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.annotations.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandProperties

@CommandProperties(
    name = "maps",
    category = Category.Game,
    aliases = ["map"],
    arguments = [
        CommandArgument("name", "Map name", false)
    ])
class MapsCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {

            ctx.replyEmbed {
                setTitle("Valorant maps", "https://playvalorant.com/en-us/maps/")
                for (map in Launcher.maps) {
                    addField(map.name.capitalize(), map.description, false)
                }
            }.queue()
        } else {
            val map = Launcher.getMap(ctx.args.first())
                ?: return ctx.reply("There is no map with this name.").queue()

            ctx.replyEmbed {
                setTitle(map.name.capitalize(), "https://playvalorant.com/en-us/maps/")
                setDescription(map.description)
                setThumbnail(map.minimap)
                setImage(map.imageUrl)
            }.queue()
        }
    }

    override val help: String = "Get a list of maps or information about a specific map from Valorant"

}