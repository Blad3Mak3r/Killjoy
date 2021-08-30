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

package dev.killjoy.commands.misc


import dev.killjoy.Launcher
import dev.killjoy.apis.memes.RedditMemes
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.utils.Emojis

@CommandProperties("meme", Category.Misc)
class MemeCommand : Command() {

    override suspend fun handle(ctx: CommandContext) {
        try {
            val meme = Launcher.cache.memes.get("ValorantMemes")

            ctx.embed {
                setTitle(meme.title, meme.permanentLink)
                setImage(meme.image)
                setFooter("\uD83D\uDC4D\uD83C\uDFFB ${meme.score} | \uD83D\uDCAC ${meme.comments}")
                setDefaultColor()
            }.queue()
        } catch (e: Exception) {
            ctx.reply(Emojis.Outage, "Cannot get any meme at the moment, try again latter...").queue()
        }
    }

    override val help = "Just Valorant related memes."
}