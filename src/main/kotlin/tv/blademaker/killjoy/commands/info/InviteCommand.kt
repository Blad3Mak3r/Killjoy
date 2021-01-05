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