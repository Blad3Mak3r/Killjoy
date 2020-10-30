package tv.blademaker.killjoy.commands.games

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis
import tv.blademaker.killjoy.utils.Utils

@CommandMeta("skills", Category.Game, aliases = ["skill"])
class SkillCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) return Utils.Commands.replyWrongUsage(ctx, this)

        val skill = Launcher.getSkills().find { it.id.equals(ctx.args[0], true) }
            ?: return ctx.send(Emojis.NoEntry, "I have not been able to find that skill...").queue()

        val agent = Launcher.agents.find { it.skills.any { s -> s === skill } }!!

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

    override val args: List<CommandArgument>
        get() = listOf(
            CommandArgument("skill_name", "A Skill name [shock-bolt]", true)
        )


}