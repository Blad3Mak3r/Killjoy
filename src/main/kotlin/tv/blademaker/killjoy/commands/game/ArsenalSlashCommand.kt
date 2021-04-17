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
import tv.blademaker.killjoy.utils.Emojis
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext

@Suppress("unused")
class ArsenalSlashCommand : AbstractSlashCommand("arsenal") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.event.acknowledge().queue()

        val selection = ctx.getOption("weapon")?.asString

        if (selection == null) {
            val arsenal = Launcher.arsenal

            ctx.sendEmbed {
                setTitle("Valorant Arsenal")
                for (weapon in arsenal) {
                    addField("${weapon.name} //${weapon.type.name.toUpperCase()}", weapon.short, true)
                }
                setFooter("If you want to get more information about an weapon use \"joy arsenal weapon_name\"")
            }.queue()
        } else {
            val weapon = Launcher.getWeapon(selection)
                ?: Launcher.getWeaponById(selection)
                ?: return ctx.send(Emojis.NoEntry, "That weapon does not exists...").queue()

            ctx.send(weapon.asEmbed()).queue()
        }
    }

}