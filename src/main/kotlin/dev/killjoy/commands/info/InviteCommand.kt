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

package dev.killjoy.commands.info

import dev.killjoy.INVITE_URL
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.utils.Emojis
import java.util.concurrent.TimeUnit

@CommandProperties("invite", Category.Information)
class InviteCommand : Command() {

    override suspend fun handle(ctx: CommandContext) {
        val content = ctx.guild.i18nCommand("invite.message", INVITE_URL)
        ctx.send(Emojis.ArrowRight, "$content\n`` This message will be deleted in 1 min. ``")
            .delay(1, TimeUnit.MINUTES)
            .flatMap {
                it.delete()
            }.queue()
    }

    override val help = "Generate a invitation link for invite Killjoy to your servers."
}