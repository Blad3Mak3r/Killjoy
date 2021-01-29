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
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandProperties

@CommandProperties("ping", Category.Information)
class PingCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        val msg = ctx.channel.sendMessage("Fetching...").await()
        val ping = ctx.jda.restPing.await()

        msg.editMessage(String.format(
                "\uD83C\uDF10 Rest: `` %d ``\n\uD83D\uDDE8️ Gateway: `` %d ``",
                ping, ctx.jda.gatewayPing)).await()
    }
}