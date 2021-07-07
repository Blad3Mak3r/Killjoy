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
import dev.killjoy.i18n.i18nCommand
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext

@Suppress("unused")
class InviteSlashCommand : AbstractSlashCommand("invite") {

    override suspend fun handle(ctx: SlashCommandContext) {
        val content = ctx.i18nCommand("invite.message", INVITE_URL)
        ctx.reply(content).setEphemeral(true).queue()
    }

}