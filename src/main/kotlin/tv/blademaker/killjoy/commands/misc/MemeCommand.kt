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