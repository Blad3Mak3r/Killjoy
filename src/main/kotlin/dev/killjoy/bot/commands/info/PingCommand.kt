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

import net.dv8tion.jda.api.EmbedBuilder
import net.hugebot.extensions.jda.await
import dev.killjoy.bot.framework.Category
import dev.killjoy.bot.framework.ColorExtra
import dev.killjoy.bot.framework.CommandContext
import dev.killjoy.bot.framework.abs.Command
import dev.killjoy.bot.framework.annotations.CommandProperties

@CommandProperties("ping", Category.Information)
class PingCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        val msg = ctx.embed { setDescription("Fetching...") } .await()
        val rest = ctx.jda.restPing.await()
        val gateway = ctx.jda.gatewayPing

        val content = String.format("\uD83C\uDF10 Rest: `` %d ``\n\n\uD83D\uDDE8️ Gateway: `` %d ``", rest, gateway)
        msg.editMessage(EmbedBuilder().setDescription(content).setColor(ColorExtra.VAL_RED).build()).queue()
    }

    override val help = "Pong!"
}