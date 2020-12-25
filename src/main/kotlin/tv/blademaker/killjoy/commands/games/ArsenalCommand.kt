/*******************************************************************************
 * Copyright (c) 2020. Blademaker
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

package tv.blademaker.killjoy.commands.games

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis
import tv.blademaker.killjoy.valorant.Weapon

@CommandMeta("arsenal", Category.Game, aliases = ["weapons", "weapon"])
class ArsenalCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            val arsenal = Launcher.arsenal

            ctx.embed {
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

            ctx.send(weapon.asEmbed().build()).queue()
        }
    }

    override val help: String
        get() = HELP

    override val args: List<CommandArgument>
        get() = ARGS

    companion object {
        private const val HELP = "Get information and statistics about a Valorant weapon or the entire aresenal."
        private val ARGS = listOf(
                CommandArgument("weapon", "A valid Valorant weapon name [tacticalknife]", false)
        )
    }
}