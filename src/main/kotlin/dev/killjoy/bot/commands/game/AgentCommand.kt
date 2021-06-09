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
import dev.killjoy.bot.framework.Category
import dev.killjoy.bot.framework.ColorExtra
import dev.killjoy.bot.framework.CommandContext
import dev.killjoy.bot.framework.abs.Command
import dev.killjoy.bot.framework.annotations.CommandArgument
import dev.killjoy.bot.framework.annotations.CommandProperties
import dev.killjoy.bot.utils.Emojis
import dev.killjoy.bot.utils.extensions.isInt

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

        if (ctx.args.isEmpty()) {
            val agents = Launcher.agents

            ctx.replyEmbed {
                setTitle("Valorant Agents")
                for (agent in agents) {
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio, true)
                }
                setFooter("If you want to get more information about an agent use \"joy agents agent_name\"")
            }.queue()

        } else if (ctx.args.size == 1) {
            val input = ctx.args[0]
            val isInt = input.isInt()

            val agent = Launcher.getAgent(input)
                ?: return if (isInt) ctx.send(Emojis.NoEntry, "Agent with id ``$input`` does not exists...").queue()
                else ctx.send(Emojis.NoEntry, "Agent with name ``${input.capitalize()}`` does not exists...").queue()

            ctx.reply(agent.asEmbed().build()).queue()
        } else {
            val input = ctx.args[0]
            val isInt = input.isInt()

            val agent = Launcher.getAgent(input)
                ?: return if (isInt) ctx.send(Emojis.NoEntry, "Agent with id ``$input`` does not exists...").queue()
                else ctx.send(Emojis.NoEntry, "Agent with name ``${input.capitalize()}`` does not exists...").queue()

            val skill = agent.skills.find { it.button.name.equals(ctx.args[1], true) }
                ?: return ctx.send(Emojis.NoEntry, "I have not been able to find that skill...").queue()

            ctx.replyEmbed {
                setAuthor(agent.name, null, agent.avatar)
                setTitle(skill.name)
                setDescription(skill.info)
                setThumbnail(skill.iconUrl)
                setImage(skill.preview)
                addField("Action Button", skill.button.name, true)
                addField("Usage Cost", skill.cost, true)
                setColor(ColorExtra.VAL_RED)
            }.queue()
        }
    }
}