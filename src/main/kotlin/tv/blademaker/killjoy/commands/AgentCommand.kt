package tv.blademaker.killjoy.commands

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis

@CommandMeta("agents", Category.Game, aliases = ["agent"])
class AgentCommand : Command() {

    override val help: String
        get() = HELP

    override fun handle(ctx: CommandContext) {

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
            val agent = Launcher.getAgent(ctx.args[0]) ?: return ctx.send(Emojis.NoEntry, "That agent does not exists...").queue()

            ctx.send(agent.asEmbed().build()).queue()
        } else {
            val agent = Launcher.getAgent(ctx.args[0]) ?: return ctx.send(Emojis.NoEntry, "That agent does not exists...").queue()

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
        private const val HELP = "Get information and statistics about a Valorant agent"
        private val ARGS = listOf(
                CommandArgument("agent_name", "An agent name [jett]", false),
                CommandArgument("skill_button", "The button used to use this skill [q]", false)
        )
    }
}