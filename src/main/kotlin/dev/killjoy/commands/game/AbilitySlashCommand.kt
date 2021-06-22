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
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.slash.api.annotations.SlashSubCommand
import kotlin.math.ceil

@Suppress("unused")
class AbilitySlashCommand : AbstractSlashCommand("ability") {

    @SlashSubCommand("info")
    override suspend fun handle(ctx: SlashCommandContext) {

        val option = ctx.getOption("name")!!.asString

        val ability = Launcher.getAbility(option)
            ?: return ctx.sendNotFound("Ability not found.").queue()

        ctx.reply(ability.asEmbed()).queue()
    }

    companion object {
        private const val MAX_ABILITIES_PER_PAGE = 5
    }
}