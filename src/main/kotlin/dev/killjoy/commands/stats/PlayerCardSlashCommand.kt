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

package dev.killjoy.commands.stats

import dev.killjoy.Launcher
import dev.killjoy.services.PlayerCard
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.Permission
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import tv.blademaker.slash.api.annotations.Permissions

@Suppress("unused")
class PlayerCardSlashCommand : AbstractSlashCommand("playercard") {

    @Permissions([], [
        Permission.MESSAGE_ATTACH_FILES
    ])
    override suspend fun handle(ctx: SlashCommandContext) {
        val user = ctx.getOption("user")?.asUser ?: ctx.author

        ctx.acknowledge().queue()

        val accountWithStats = Launcher.database.account.findByUserAsync(user).await()
            ?: return ctx.sendNotFound("${user.asTag} is not registered on Killjoy.").queue()

        val agentName = accountWithStats.stats?.mostPlayedAgent?.lowercase() ?: "killjoy"

        val agent = Launcher.getAgent(agentName)
            ?: error("Agent with name $agentName not found.")

        val image = PlayerCard.generate(accountWithStats, agent).await()

        ctx.hook.sendFile(image, "playerCard-${user.asTag}.png").queue()
    }

}