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

package dev.killjoy.bot.commands.pugs

import dev.killjoy.bot.Launcher
import dev.killjoy.bot.utils.Emojis
import dev.killjoy.database.enums.ClosePugResult
import dev.killjoy.database.enums.CreatePugResult
import dev.killjoy.database.enums.JoinPugResult
import dev.killjoy.database.enums.LeavePugResult
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.slash.api.annotations.Permissions
import dev.killjoy.slash.api.annotations.SlashSubCommand
import dev.killjoy.slash.utils.SlashUtils.asEphemeral
import net.dv8tion.jda.api.Permission

@Suppress("unused")
class PugsSlashCommand : AbstractSlashCommand("pugs") {

    @SlashSubCommand("info")
    suspend fun info(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()
        val pug = Launcher.database.pugs.findByGuild(ctx.guild)
            ?: return ctx.send(Emojis.NoEntry, NOT_ACTIVE_PUG).setEphemeral(true).queue()

        ctx.send(pug.asEmbed()).queue()
    }

    @SlashSubCommand("join")
    suspend fun join(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()
        when (Launcher.database.pugs.joinPug(ctx.guild, ctx.author)) {
            JoinPugResult.CantJoin -> {
                ctx.send(Emojis.NoEntry, "You have not been able to join the PUG.").asEphemeral().queue()
            }
            JoinPugResult.AlreadyJoined -> {
                ctx.send(Emojis.Success, "You are **already registered** in the PUG.").asEphemeral().queue()
            }
            JoinPugResult.Joined -> {
                ctx.send(Emojis.Success, "You have **joined** the PUG.").queue()
            }
            JoinPugResult.PugDoesNotExists -> {
                ctx.send(Emojis.NoEntry, NOT_ACTIVE_PUG).asEphemeral().queue()
            }
        }
    }

    @SlashSubCommand("leave")
    suspend fun leave(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        when (Launcher.database.pugs.leavePug(ctx.guild, ctx.author)) {
            LeavePugResult.CantLeft ->
                ctx.send(Emojis.NoEntry, "You have not been able to leave the PUG.").asEphemeral().queue()

            LeavePugResult.AlreadyLeft ->
                ctx.send(Emojis.Success, "You are **not registered** in the PUG.").asEphemeral().queue()

            LeavePugResult.Left ->
                ctx.send(Emojis.Success, "You have **left** the PUG.").queue()

            LeavePugResult.PugDoesNotExists ->
                ctx.send(Emojis.NoEntry, NOT_ACTIVE_PUG).asEphemeral().queue()
        }
    }

    @Permissions(user = [Permission.MANAGE_SERVER])
    @SlashSubCommand("teams")
    suspend fun teams(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        val pug = Launcher.database.pugs.findByGuild(ctx.guild)
            ?: return ctx.send(Emojis.NoEntry, NOT_ACTIVE_PUG).queue()

        val players = pug.taggedPlayers

        if (players.size < 4)
            return ctx.send(Emojis.NoEntry, "There are **not enough players** to create the teams, " +
                    "only **${players.size}** players are registered in the PUG.").queue()

        val redTeam = mutableListOf<String>()
        val blueTeam = mutableListOf<String>()

        for ((index, player) in players.shuffled().withIndex()) {
            if (index%2==0) redTeam.add(player)
            else blueTeam.add(player)
        }

        ctx.sendEmbed {
            setTitle("Generated teams for the active PUG")
            addField("Team Red", redTeam.joinToString("\n"), true)
            addField("Team Blue", blueTeam.joinToString("\n"), true)
        }.queue()
    }

    @Permissions(user = [Permission.MANAGE_SERVER])
    @SlashSubCommand("create")
    suspend fun create(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        when (Launcher.database.pugs.create(ctx.guild, ctx.author)) {
            CreatePugResult.Opened ->
                ctx.send(Emojis.Success, "A new PUG has been created for this guild.").queue()

            CreatePugResult.CantOpen ->
                ctx.send(Emojis.NoEntry, "It was not possible to create a PUG at this time.").queue()

            CreatePugResult.AlreadyActivePug ->
                ctx.send(Emojis.NoEntry, "There is currently an active PUG for this server.").queue()

        }
    }

    @Permissions(user = [Permission.MANAGE_SERVER])
    @SlashSubCommand("close")
    suspend fun close(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        when (Launcher.database.pugs.close(ctx.guild)) {
            ClosePugResult.CantClose ->
                ctx.send(Emojis.NoEntry, "Cannot close the active PUG at the moment.").setEphemeral(true).queue()
            ClosePugResult.Closed ->
                ctx.send(Emojis.Success, "Active PUG **has been closed** successfully.").setEphemeral(true).queue()
            ClosePugResult.NotActivePug ->
                ctx.send(Emojis.NoEntry, NOT_ACTIVE_PUG).queue()
        }
    }

    companion object {
        private const val NOT_ACTIVE_PUG = "**There is not any active PUG on this guild at the moment.**"
    }

}