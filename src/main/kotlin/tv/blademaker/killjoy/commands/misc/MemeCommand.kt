package tv.blademaker.killjoy.commands.misc


import net.hugebot.memes4k.Memes4K
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.utils.Emojis

@CommandMeta("meme", Category.Misc)
class MemeCommand : Command() {

    override suspend fun handle(ctx: CommandContext) {
        val meme = Memes4K.getMeme("ValorantMemes")
            ?: return ctx.reply(Emojis.Outage, "Cannot get any meme at the moment, try again latter...").queue()

        ctx.embed {
            setTitle(meme.title, meme.permanentLink)
            setImage(meme.image)
            setFooter("\uD83D\uDC4D\uD83C\uDFFB ${meme.score} | \uD83D\uDCAC ${meme.comments}")
            setColor(ColorExtra.VAL_RED)
        }.queue()
    }

    override val help: String
        get() = "Just Valorant related memes."
}