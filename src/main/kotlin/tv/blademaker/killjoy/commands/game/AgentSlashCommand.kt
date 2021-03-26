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

package tv.blademaker.killjoy.commands.game

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Command
import net.hugebot.extensions.isInt
import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.slash.AbstractSlashCommand
import tv.blademaker.killjoy.slash.SlashCommandContext
import tv.blademaker.killjoy.slash.SlashCommandOption
import tv.blademaker.killjoy.valorant.ValorantAgent

@Suppress("unused")
class AgentSlashCommand : AbstractSlashCommand("agents") {

    override suspend fun handle(ctx: SlashCommandContext) {
        val embed = EmbedBuilder().apply {
            setColor(ColorExtra.VAL_RED)
            setTitle("Valorant Agents")
            for (agent in Launcher.agents) {
                addField("${agent.role.emoji} - ${agent.name}", agent.bio, true)
            }
        }.build()
        ctx.event.acknowledge().addEmbeds(embed).queue()
    }

    @SlashCommandOption(Command.OptionType.STRING)
    suspend fun agent(ctx: SlashCommandContext) {
        ctx.event.acknowledge().queue()
        val agentName = ctx.getOption("agent")!!.asString
        val skillName = ctx.getOption("skill")?.asString

        val agent = findAgent(agentName)
            ?: return ctx.hook.sendMessage("Agent with name or number ``$agentName`` does not exists.").queue()

        if (skillName != null) {

            val skill = agent.skills.find { it.button.name.equals(skillName, true) }
                ?: return ctx.hook.sendMessage("I have not been able to find that skill...").queue()

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

            ctx.hook.sendMessage(embed).queue()

        } else {
            ctx.hook.sendMessage(agent.asEmbed().build()).queue()
        }
    }

    companion object {
        private fun findAgent(input: String): ValorantAgent? {
            return Launcher.retrieveAgentByInput(input)
        }
    }
}