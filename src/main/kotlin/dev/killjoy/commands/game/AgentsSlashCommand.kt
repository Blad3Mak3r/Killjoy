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

@file:Suppress("DuplicatedCode")

package dev.killjoy.commands.game

import dev.killjoy.Launcher
import dev.killjoy.extensions.info
import dev.killjoy.extensions.jda.betterString
import dev.killjoy.extensions.jda.ktx.await
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.utils.ParseUtils
import dev.killjoy.utils.paginationButtons
import dev.killjoy.utils.userInteractionFilter
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.BaseSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil

@Suppress("unused")
class AgentsSlashCommand : BaseSlashCommand("agents") {

    override suspend fun handle(ctx: SlashCommandContext) {

        val interactionID = ctx.hook.interaction.id

        val agents = Launcher.agents.sortedBy { it.name }

        val page = ctx.getOption("page")?.asString?.toInt() ?: 1
        val totalPages = ceil((agents.size.toFloat() / MAX_AGENTS_PER_PAGE.toFloat())).toInt()
        var pageIndex = ParseUtils.getPageIndex(page, totalPages)

        fun buildEmbed(): MessageEmbed {
            val firstIndex = if (pageIndex <= 0) 0 else (pageIndex * MAX_AGENTS_PER_PAGE)
            val lastIndex = (firstIndex + MAX_AGENTS_PER_PAGE).coerceAtMost(agents.size)
            val f0 = pageIndex + 1
            val f2 = firstIndex + 1
            val f4 = agents.size

            return EmbedBuilder().apply {
                setTitle("Valorant Agents")
                setDefaultColor()
                for (index in firstIndex until lastIndex) {
                    val agent = agents[index]
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio(ctx.guild), false)
                }
                setFooter(ctx.i18nCommand("abilities.footer", f0, totalPages, f2, lastIndex, f4))
            }.build()
        }

        val enabledButtons = paginationButtons(ctx, false)
        val disabledButtons = paginationButtons(ctx, true)

        ctx.reply(buildEmbed()).addActionRows(enabledButtons).queue()

        val buttonsState = AtomicBoolean(true)

        fun stop(event: ButtonClickEvent? = null) {
            buttonsState.set(false)
            if (event != null) event.editComponents(disabledButtons).queue({}, {})
            else ctx.hook.editOriginalComponents(disabledButtons).queue({}, {})
        }

        while (buttonsState.get()) {
            withTimeoutOrNull(60000) {
                val pressed = ctx.await<ButtonClickEvent> { userInteractionFilter(it, ctx.author, interactionID) }
                when (pressed.componentId.split(":")[1]) {
                    "preview" -> {
                        if (pageIndex > 0) {
                            pageIndex -= 1
                            pressed.editMessageEmbeds(buildEmbed()).queue()
                        } else pressed.deferEdit().queue()
                    }
                    "next" -> {
                        if (pageIndex < (totalPages - 1)) {
                            pageIndex += 1
                            pressed.editMessageEmbeds(buildEmbed()).queue()
                        } else pressed.deferEdit().queue()
                    }
                    "cancel" -> {
                        logButtonFinalAction(ctx, "canceled")
                        stop(pressed)
                    }
                }
            } ?: run {
                logButtonFinalAction(ctx, "timed out")
                stop()
            }
        }
    }

    companion object {
        private const val MAX_AGENTS_PER_PAGE = 5

        private val logger = LoggerFactory.getLogger(AgentsSlashCommand::class.java)

        private fun logButtonFinalAction(ctx: SlashCommandContext, trigger: String) {
            logger.info(ctx.guild, "Agents buttons ${trigger.lowercase()} for ${ctx.author}//${ctx.hook.interaction.betterString()}")
        }
    }
}