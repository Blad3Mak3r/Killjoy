package tv.blademaker.killjoy.commands.games

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis

@CommandMeta("arsenal", Category.Game, aliases = ["weapons", "weapon"])
class ArsenalCommand : Command() {
    override fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            val arsenal = Launcher.arsenal

            ctx.embed {
                setTitle("Valorant Arsenal")
                for (weapon in arsenal) {
                    addField("${weapon.name} //${weapon.type.name.toUpperCase()}", weapon.short, true)
                }
                setFooter("If you want to get more information about an weapon use \"joy arsenal weapon_name\"")
            }.queue()

        } else {
            val weapon = Launcher.getWeaponById(ctx.args[0]) ?: return ctx.send(Emojis.NoEntry, "That weapon does not exists...").queue()

            ctx.send(weapon.asEmbed().build()).queue()
        }
    }

    override val help: String
        get() = HELP

    override val args: List<CommandArgument>
        get() = ARGS

    companion object {
        private const val HELP = "Get information and statistics about a Valorant weapon or the entire aresenal."
        private val ARGS = listOf(
                CommandArgument("weapon", "A valid Valorant weapon name [tacticalknife]", false)
        )
    }
}