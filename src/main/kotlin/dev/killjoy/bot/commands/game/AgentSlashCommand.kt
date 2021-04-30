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

import net.dv8tion.jda.api.EmbedBuilder
import dev.killjoy.bot.Launcher
import dev.killjoy.bot.framework.ColorExtra
import dev.killjoy.bot.valorant.ValorantAgent
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext

@Suppress("unused")
class AgentSlashCommand : AbstractSlashCommand("agents") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        val agentName = ctx.getOption("agent")?.asString
        val skillName = ctx.getOption("skill")?.asString

        if (agentName == null && skillName == null) {

            val embed = EmbedBuilder().apply {
                setColor(ColorExtra.VAL_RED)
                setTitle("Valorant Agents")
                for (agent in Launcher.agents) {
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio, true)
                }
            }.build()

            ctx.send(embed).queue()

        } else if (agentName != null && skillName == null) {

            val agent = findAgent(agentName)
                ?: return ctx.sendNotFound("Agent with name or number ``$agentName`` does not exists.").queue()

            ctx.send(agent.asEmbed()).queue()

        } else if (agentName != null && skillName != null) {

            val agent = findAgent(agentName)
                ?: return ctx.sendNotFound("Agent with name or number ``$agentName`` does not exists.").queue()

            val skill = agent.skills.find { it.button.name.equals(skillName, true) }
                ?: return ctx.sendNotFound("I have not been able to find that skill...").queue()

            val embed = EmbedBuilder().apply {
                setAuthor(agent.name, null, agent.avatar)
                setTitle(skill.name)
                setDescription(skill.info)
                setThumbnail(skill.iconUrl)
                setImage(skill.preview)
                addField("Action Button", skill.button.name, true)
                addField("Usage Cost", skill.cost, true)
                setColor(ColorExtra.VAL_RED)
            }.build()

            ctx.send(embed).queue()

        } else {

            ctx.send("In order to see the abilities of the agents you need to choose an agent first.").queue()

        }
    }

    companion object {
        private fun findAgent(input: String): ValorantAgent? {
            return Launcher.getAgent(input)
        }
    }
}