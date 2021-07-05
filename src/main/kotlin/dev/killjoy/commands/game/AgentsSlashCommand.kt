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
import dev.killjoy.extensions.jda.await
import dev.killjoy.extensions.jda.betterString
import dev.killjoy.extensions.jda.ktx.await
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.utils.ParseUtils
import dev.killjoy.utils.buildPaginationActionRow
import dev.killjoy.valorant.agent.ValorantAgent
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil

@Suppress("unused")
class AgentsSlashCommand : AbstractSlashCommand("agents") {

    override suspend fun handle(ctx: SlashCommandContext) {

        val interactionID = ctx.hook.interaction.id

        val agents = Launcher.agents.sortedBy { it.name }

        val page = ctx.getOption("page")?.asString?.toInt() ?: 1
        val totalPages = ceil((agents.size.toFloat() / MAX_AGENTS_PER_PAGE.toFloat())).toInt()
        var pageIndex = ParseUtils.getPageIndex(page, totalPages)

        ctx.reply(buildEmbed(ctx, agents, pageIndex, totalPages)).addActionRows(buildPaginationActionRow(ctx)).queue()

        val enabledButtons = AtomicBoolean(true)

        suspend fun stop() {
            enabledButtons.set(false)
            ctx.hook.editOriginalComponents(emptyList()).await()
        }

        while (enabledButtons.get()) {
            withTimeoutOrNull(60000) {
                val pressed = ctx.await<ButtonClickEvent> {
                    it.guild?.idLong == ctx.guild.idLong &&
                            it.componentId.split(":")[0] == interactionID &&
                            it.channel.idLong == ctx.channel.idLong &&
                            it.user.idLong == ctx.author.idLong
                }
                pressed.deferEdit().queue()
                when (pressed.componentId.split(":")[1]) {
                    "preview" -> {
                        if (pageIndex > 0) {
                            pageIndex -= 1
                            ctx.hook.editOriginalEmbeds(
                                buildEmbed(
                                    ctx,
                                    agents,
                                    pageIndex,
                                    totalPages
                                )
                            ).queue()
                        }
                    }
                    "next" -> {
                        if (pageIndex < (totalPages - 1)) {
                            pageIndex += 1
                            ctx.hook.editOriginalEmbeds(
                                buildEmbed(
                                    ctx,
                                    agents,
                                    pageIndex,
                                    totalPages
                                )
                            ).queue()
                        }
                    }
                    "cancel" -> {
                        logButtonFinalAction(ctx, "canceled")
                        stop()
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

        private fun buildEmbed(ctx: SlashCommandContext, agents: List<ValorantAgent>, pageIndex: Int = 0, totalPages: Int): MessageEmbed {
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
    }
}