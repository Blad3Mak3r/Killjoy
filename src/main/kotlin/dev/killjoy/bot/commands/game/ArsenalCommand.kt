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

package dev.killjoy.bot.commands.game

import dev.killjoy.bot.Launcher
import dev.killjoy.bot.framework.Category
import dev.killjoy.bot.framework.CommandContext
import dev.killjoy.bot.framework.abs.Command
import dev.killjoy.bot.framework.annotations.CommandArgument
import dev.killjoy.bot.framework.annotations.CommandProperties
import dev.killjoy.bot.utils.Emojis

@CommandProperties(
    name = "arsenal",
    category = Category.Game,
    aliases = ["weapons", "weapon"],
    arguments = [
        CommandArgument("weapon", "A valid Valorant weapon name [tacticalknife]", false)
    ]
)
class ArsenalCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            val arsenal = Launcher.arsenal

            ctx.replyEmbed {
                setTitle("Valorant Arsenal")
                for (weapon in arsenal) {
                    addField("${weapon.name} //${weapon.type.name.toUpperCase()}", weapon.short, true)
                }
                setFooter("If you want to get more information about an weapon use \"joy arsenal weapon_name\"")
            }.queue()

        } else {
            val weapon = Launcher.getWeapon(ctx.args.joinToString(" "))
                ?: Launcher.getWeaponById(ctx.args[0])
                ?: return ctx.send(Emojis.NoEntry, "That weapon does not exists...").queue()

            ctx.reply(weapon.asEmbed().build()).queue()
        }
    }

    override val help: String = "Get information and statistics about a Valorant weapon or the entire arsenal."
}