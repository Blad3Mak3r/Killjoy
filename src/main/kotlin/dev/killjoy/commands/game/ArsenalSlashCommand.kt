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
import dev.killjoy.extensions.slash.sendNotFound
import dev.killjoy.i18n.i18nCommand
import tv.blademaker.slash.api.BaseSlashCommand
import tv.blademaker.slash.api.SlashCommandContext

@Suppress("unused")
class ArsenalSlashCommand : BaseSlashCommand("arsenal") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.event.deferReply().queue()

        val selection = ctx.getOption("weapon")?.asString

        if (selection == null) {
            val arsenal = Launcher.arsenal

            ctx.sendEmbed {
                setTitle(ctx.i18nCommand("arsenal.title"))
                for (weapon in arsenal) {
                    addField("${weapon.name(ctx.guild)} //${weapon.type(ctx.guild)}", weapon.short(ctx.guild), true)
                }
            }.queue()
        } else {
            val weapon = Launcher.getWeapon(selection)
                ?: return ctx.sendNotFound(ctx.i18nCommand("arsenal.notFound")).queue()

            ctx.send(weapon.asEmbed(ctx.guild)).queue()
        }
    }

}