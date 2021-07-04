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
import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.valorant.agent.ValorantAgent
import net.dv8tion.jda.api.entities.MessageEmbed

@Suppress("unused")
class AgentSlashCommand : AbstractSlashCommand("agent") {

    override suspend fun handle(ctx: SlashCommandContext) {

        fun send(content: MessageEmbed) {
            if (ctx.isAcknowledged) ctx.send(content).queue()
            else ctx.reply(content).queue()
        }

        fun send(content: String) {
            if (ctx.isAcknowledged) ctx.send(content).queue()
            else ctx.reply(content).queue()
        }

        val isCached = RiotAPI.AgentStatsAPI.cached

        val agentName = ctx.getOption("name")!!.asString

        if (!isCached) ctx.acknowledge().queue()

        val agent = findAgent(agentName)
            ?: return sendAgentNotFound(ctx, agentName)

        val embed = agent.asEmbed(ctx.guild).build()
        send(embed)
    }

    companion object {
        private fun sendAgentNotFound(ctx: SlashCommandContext, agentName: String) {
            val content = ctx.i18nCommand("agent.notFound", agentName)
            ctx.sendNotFound(content).queue()
        }

        private fun findAgent(input: String): ValorantAgent? {
            return Launcher.getAgent(input)
        }
    }
}