package tv.blademaker.killjoy.commands

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis

@CommandMeta("agents", Category.Information, aliases = ["agent"])
class AgentCommand : Command() {
    override fun handle(ctx: CommandContext) {

        if (ctx.args.isEmpty()) {
            val agents = Launcher.getAgents()

            ctx.embed {
                setTitle("Valorant Agents")
                for (agent in agents) {
                    addField("${agent.role.emoji} - ${agent.name}", agent.bio, true)
                }
                setFooter("If you want to get more information about an agent use \"joy agents agent_name\"")
            }.queue()

        } else {
            val agent = Launcher.getAgent(ctx.args[0]) ?: return ctx.send(Emojis.NoEntry, "That agent does not exists...").queue()

            ctx.send(agent.asEmbed().build()).queue()
        }
    }

    override val args: List<CommandArgument>
        get() = listOf(
            CommandArgument("agent_name", "An agent name [jett]", false)
        )
}