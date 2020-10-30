package tv.blademaker.killjoy.commands.info

import net.hugebot.extensions.jda.await
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta

@CommandMeta("ping", Category.Information)
class PingCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        val msg = ctx.channel.sendMessage("Fetching...").await()
        val ping = ctx.jda.restPing.await()

        msg.editMessage(String.format(
                "\uD83C\uDF10 Rest: `` %d ``\n\uD83D\uDDE8️ Gateway: `` %d ``",
                ping, ctx.jda.gatewayPing)).await()
    }
}