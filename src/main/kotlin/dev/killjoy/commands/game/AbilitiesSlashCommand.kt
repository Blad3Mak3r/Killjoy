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
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.slash.api.annotations.SlashSubCommand
import kotlin.math.ceil

@Suppress("unused")
class AbilitiesSlashCommand : AbstractSlashCommand("abilities") {

    @SlashSubCommand("all")
    override suspend fun handle(ctx: SlashCommandContext) {

        val abilities = Launcher.getAbilities().sortedBy { it.name(ctx.guild) }

        val page = ctx.getOption("page")?.asString?.toInt() ?: 1

        val totalPages = ceil((abilities.size.toFloat() / MAX_ABILITIES_PER_PAGE.toFloat())).toInt()

        val pageIndex = getPageIndex(page, totalPages)
        val firstIndex = if (pageIndex <= 0) 0 else (pageIndex * MAX_ABILITIES_PER_PAGE)
        val lastIndex = (firstIndex + MAX_ABILITIES_PER_PAGE).coerceAtMost(abilities.size)

        ctx.replyEmbed {
            setTitle(ctx.i18n(I18nKey.VALORANT_ABILITIES_TITLE))
            setDescription("")
            for (index in firstIndex until lastIndex) {
                val ability = abilities[index]

                val body = buildString {
                    appendLine(ability.description(ctx.guild))
                    appendLine("**${ctx.i18n(I18nKey.ABILITY_COST)}**: ${ability.cost}")
                }
                addField("${ability.name(ctx.guild)} (${ability.agent.name})", body, false)
            }
            val f0 = pageIndex + 1
            val f2 = firstIndex + 1
            val f4 = abilities.size
            setFooter(ctx.i18nCommand("abilities.footer", f0, totalPages, f2, lastIndex, f4))
        }.queue()
    }

    private fun getPageIndex(page: Int, totalPages: Int): Int {
        return if (totalPages <= 0 || page <= 1) 0
        else page.coerceAtMost(totalPages) - 1
    }

    companion object {
        private const val MAX_ABILITIES_PER_PAGE = 5
    }
}