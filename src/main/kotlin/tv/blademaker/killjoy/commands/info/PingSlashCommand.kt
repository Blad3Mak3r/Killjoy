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

import net.hugebot.extensions.jda.await
import tv.blademaker.killjoy.utils.Emojis
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext

class PingSlashCommand : AbstractSlashCommand("ping") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.acknowledge(true).queue()

        try {
            val ping = ctx.jda.restPing.await()
            val gatewayPing = ctx.jda.gatewayPing
            ctx.send(
                Emojis.PING_PONG,
                "**Pong!**\n\n\uD83C\uDF10 Rest ping ``${ping}ms``\n\uD83D\uDDE8️ Gateway ping ``${gatewayPing}ms``"
            ).queue()
        } catch (e: Exception) {
            ctx.send(Emojis.NoEntry, "Cannot fetch ping at this moment, error code: ${e.message ?: "null"}")
                .queue()
        }
    }

}