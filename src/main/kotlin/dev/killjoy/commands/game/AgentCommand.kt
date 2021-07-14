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
import dev.killjoy.extensions.capital
import dev.killjoy.extensions.jda.isInt
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandArgument
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.utils.Emojis

@CommandProperties(
    name = "agents",
    category = Category.Game,
    aliases = ["agent"],
    arguments = [
        CommandArgument("agent_name:agent_id", "An agent name [jett] or agent id [1-13]", false),
        CommandArgument("skill_button", "The button used to use this skill [q]", false)
    ]
)
class AgentCommand : Command() {

    override val help: String = "Get information and statistics about a Valorant agent."

    override suspend fun handle(ctx: CommandContext) {

        val guildLang = ctx.guild.supportedLocale.language

        if (ctx.args.isEmpty()) {
            val agents = Launcher.agents

            ctx.replyEmbed {
                setTitle("Valorant Agents")
                for (agent in agents) {
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio(ctx.guild), true)
                }
                setFooter("If you want to get more information about an agent use \"joy agents agent_name\"")
            }.queue()

        } else if (ctx.args.size == 1) {
            val input = ctx.args[0]
            val isInt = input.isInt()

            val agent = Launcher.getAgent(input)
                ?: return if (isInt) ctx.send(Emojis.NoEntry, "Agent with id ``$input`` does not exists...").queue()
                else ctx.send(Emojis.NoEntry, "Agent with name ``${input.capital(ctx.guild.supportedLocale)}`` does not exists...").queue()

            ctx.reply(agent.asEmbed(ctx.guild).build()).queue()
        } else {
            val input = ctx.args[0]
            val isInt = input.isInt()

            val agent = Launcher.getAgent(input)
                ?: return if (isInt) ctx.send(Emojis.NoEntry, "Agent with id ``$input`` does not exists...").queue()
                else ctx.send(Emojis.NoEntry, "Agent with name ``${input.capital(ctx.guild.supportedLocale)}`` does not exists...").queue()

            val skill = agent.abilities.find { it.hasButton(ctx.args[1]) }
                ?: return ctx.send(Emojis.NoEntry, "I have not been able to find that skill...").queue()

            ctx.replyEmbed {
                setAuthor(agent.name, null, agent.avatar)
                setTitle(skill.name[guildLang])
                setDescription(skill.description[guildLang])
                setThumbnail(skill.iconUrl)
                setImage(skill.preview)
                addField("Action Button", skill.button(ctx.guild), true)
                addField("Usage Cost", skill.cost, true)
                setDefaultColor()
            }.queue()
        }
    }
}