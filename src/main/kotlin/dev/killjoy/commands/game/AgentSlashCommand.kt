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
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.valorant.ValorantAgent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

@Suppress("unused")
class AgentSlashCommand : AbstractSlashCommand("agents") {

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

        val agentName = ctx.getOption("agent")?.asString
        val skillName = ctx.getOption("skill")?.asString

        if (agentName == null && skillName == null) {

            val embed = EmbedBuilder().apply {
                setDefaultColor()
                setTitle("Valorant Agents")
                for (agent in Launcher.agents) {
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio, true)
                }
            }.build()

            send(embed)

        } else if (agentName != null && skillName == null) {

            if (!isCached) ctx.acknowledge().queue()

            val agent = findAgent(agentName)
                ?: return ctx.sendNotFound("Agent with name or number ``$agentName`` does not exists.").queue()

            val embed = agent.asEmbed().build()
            send(embed)

        } else {

            send("In order to see the abilities of the agents you need to choose an agent first.")

        }
    }

    companion object {
        private fun findAgent(input: String): ValorantAgent? {
            return Launcher.getAgent(input)
        }
    }
}