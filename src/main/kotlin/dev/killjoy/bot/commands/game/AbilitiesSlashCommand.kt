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
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import kotlin.math.ceil

@Suppress("unused")
class AbilitiesSlashCommand : AbstractSlashCommand("abilities") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        val abilities = Launcher.getAbilities().sortedBy { it.skill.name }

        val page = ctx.getOption("page")?.asString?.toInt() ?: 1

        val totalPages = ceil((abilities.size.toFloat() / MAX_ABILITIES_PER_PAGE.toFloat())).toInt()

        val pageIndex = getPageIndex(page, totalPages)
        val firstIndex = if (pageIndex <= 0) 0 else (pageIndex * MAX_ABILITIES_PER_PAGE)
        val lastIndex = (firstIndex + MAX_ABILITIES_PER_PAGE).coerceAtMost(abilities.size)

        ctx.sendEmbed {
            setTitle("Valorant Abilities")
            setDescription("")
            for (index in firstIndex until lastIndex) {
                val ability = abilities[index]

                val body = buildString {
                    appendLine(ability.skill.info)
                    appendLine("**Cost**: ${ability.skill.cost}")
                }
                addField("${ability.skill.name} (${ability.agent.name})", body, false)
            }
            setFooter("Page ${pageIndex + 1} / $totalPages | Showing ${firstIndex + 1} - $lastIndex of ${abilities.size} abilities.")
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