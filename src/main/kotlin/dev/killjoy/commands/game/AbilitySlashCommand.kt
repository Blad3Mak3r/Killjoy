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
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import tv.blademaker.slash.api.BaseSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import tv.blademaker.slash.api.annotations.SlashCommandOption

@Suppress("unused")
class AbilitySlashCommand : BaseSlashCommand("ability") {

    @SlashCommandOption("info")
    override suspend fun handle(ctx: SlashCommandContext) {

        val option = ctx.getOption("name")!!.asString

        Launcher.getAbility(option)?.also { ctx.reply(it.asEmbed(ctx.guild)).queue() }
            ?: return ctx.sendNotFound(ctx.i18n(I18nKey.ABILITY_NOT_FOUND, option)).queue()
    }

    companion object {
        private const val MAX_ABILITIES_PER_PAGE = 5
    }
}