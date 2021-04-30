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

package dev.killjoy.bot.commands.info

import dev.killjoy.bot.INVITE_URL
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext

@Suppress("unused")
class InviteSlashCommand : AbstractSlashCommand("invite") {

    override suspend fun handle(ctx: SlashCommandContext) {
        val content = "Here is the invitation link to invite me to your servers:\n$INVITE_URL"
        ctx.acknowledge(true).setContent(content).queue()
    }

}