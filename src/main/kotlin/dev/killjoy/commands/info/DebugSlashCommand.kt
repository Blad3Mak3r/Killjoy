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

import dev.killjoy.Launcher
import dev.killjoy.Versions
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import dev.killjoy.utils.ParseUtils
import java.lang.management.ManagementFactory

class DebugSlashCommand : AbstractSlashCommand("debug") {

    override suspend fun handle(ctx: SlashCommandContext) {
        val guild = ctx.guild
        val uptime = ParseUtils.millisToUptime(ManagementFactory.getRuntimeMXBean().uptime)

        ctx.replyMessage {
            appendLine("**Server**: ``$guild``")
            appendLine("**Current Shard:** ``[${ctx.jda.shardInfo.shardId}/${Launcher.getShardManager().shardsTotal}]``")
            appendLine("**Current Shard Servers:** ``${Launcher.getGuildsByShard(ctx.jda.shardInfo.shardId)}``")
            appendLine("**Current Shard Members:** ``${Launcher.getTotalMembersByShard(ctx.jda.shardInfo.shardId)}``")
            appendLine("**Current Node Servers:** ``${Launcher.getTotalGuilds()}``")
            appendLine("**Current Node Members:** ``${Launcher.getTotalMembers()}``")
            appendLine("**Uptime:** ``$uptime``")
            appendLine()
            appendLine("__**• • • • Versions • • • •**__")
            appendLine("**Killjoy Version:** ``${Versions.KILLJOY}``")
            appendLine("**JDA Version:** ``${Versions.JDA}``")
        }.setEphemeral(true).queue()
    }

}