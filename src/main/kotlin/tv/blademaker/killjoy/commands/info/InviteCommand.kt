package tv.blademaker.killjoy.commands.info

import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis
import java.util.concurrent.TimeUnit

@CommandMeta("invite", Category.Information)
class InviteCommand : Command() {

    override suspend fun handle(ctx: CommandContext) {
        ctx.send(Emojis.ArrowRight, "Here is the invitation link to invite me to your servers:\n$INVITE\n`` This message will be deleted in 1 min. ``")
            .delay(1, TimeUnit.MINUTES)
            .flatMap {
                it.delete()
            }.queue()
    }

    override val help: String
        get() = HELP

    companion object {
        const val HELP = "Generate a invitation link for invite Killjoy to your servers."
        const val INVITE = "https://discord.com/api/oauth2/authorize?client_id=706887214088323092&permissions=321600&scope=bot"
    }
}