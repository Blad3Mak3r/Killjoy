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

package tv.blademaker.killjoy.commands.games

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis
import tv.blademaker.killjoy.utils.extensions.isInt

@CommandMeta("agents", Category.Game, aliases = ["agent"])
class AgentCommand : Command() {

    override val help: String
        get() = HELP

    override suspend fun handle(ctx: CommandContext) {

        if (ctx.args.isEmpty()) {
            val agents = Launcher.agents

            ctx.embed {
                setTitle("Valorant Agents")
                for (agent in agents) {
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio, true)
                }
                setFooter("If you want to get more information about an agent use \"joy agents agent_name\"")
            }.queue()

        } else if (ctx.args.size == 1) {
            val input = ctx.args[0]
            val isInt = input.isInt()

            val agent = Launcher.retrieveAgentByInput(input)
                ?: return if (isInt) ctx.send(Emojis.NoEntry, "Agent with id ``$input`` does not exists...").queue()
                else ctx.send(Emojis.NoEntry, "Agent with name ``${input.capitalize()}`` does not exists...").queue()

            ctx.send(agent.asEmbed().build()).queue()
        } else {
            val input = ctx.args[0]
            val isInt = input.isInt()

            val agent = Launcher.retrieveAgentByInput(input)
                ?: return if (isInt) ctx.send(Emojis.NoEntry, "Agent with id ``$input`` does not exists...").queue()
                else ctx.send(Emojis.NoEntry, "Agent with name ``${input.capitalize()}`` does not exists...").queue()

            val skill = agent.skills.find { it.button.name.equals(ctx.args[1], true) }
                ?: return ctx.send(Emojis.NoEntry, "I have not been able to find that skill...").queue()

            ctx.embed {
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

    override val args: List<CommandArgument>
        get() = ARGS

    companion object {
        private const val HELP = "Get information and statistics about a Valorant agent."
        private val ARGS = listOf(
                CommandArgument("agent_name:agent_id", "An agent name [jett] or agent id [1-13]", false),
                CommandArgument("skill_button", "The button used to use this skill [q]", false)
        )
    }
}