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

package dev.killjoy.commands.pugs

import dev.killjoy.Launcher
import dev.killjoy.database.enums.ClosePugResult
import dev.killjoy.database.enums.CreatePugResult
import dev.killjoy.database.enums.JoinPugResult
import dev.killjoy.database.enums.LeavePugResult
import dev.killjoy.i18n.i18nCommand
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import tv.blademaker.slash.api.annotations.Permissions
import tv.blademaker.slash.api.annotations.SlashSubCommand
import tv.blademaker.slash.utils.SlashUtils.asEphemeral
import dev.killjoy.utils.Emojis
import net.dv8tion.jda.api.Permission

@Suppress("unused")
class PugsSlashCommand : AbstractSlashCommand("pugs") {

    @SlashSubCommand("current")
    suspend fun current(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()
        val pug = Launcher.database.pugs.findByGuild(ctx.guild)
            ?: return ctx.send(Emojis.NoEntry, notActivePug(ctx)).setEphemeral(true).queue()

        ctx.send(pug.asEmbed(ctx.guild)).queue()
    }

    @SlashSubCommand("join")
    suspend fun join(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()
        when (Launcher.database.pugs.joinPug(ctx.guild, ctx.author)) {
            JoinPugResult.CantJoin -> {
                ctx.send(Emojis.NoEntry, ctx.i18nCommand("pugs.join.cantJoin")).asEphemeral().queue()
            }
            JoinPugResult.AlreadyJoined -> {
                ctx.send(Emojis.Success, ctx.i18nCommand("pugs.join.alreadyJoined")).asEphemeral().queue()
            }
            JoinPugResult.Joined -> {
                ctx.send(Emojis.Success, ctx.i18nCommand("pugs.join.joined")).queue()
            }
            JoinPugResult.PugDoesNotExists -> {
                ctx.send(Emojis.NoEntry, notActivePug(ctx)).asEphemeral().queue()
            }
        }
    }

    @SlashSubCommand("leave")
    suspend fun leave(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        when (Launcher.database.pugs.leavePug(ctx.guild, ctx.author)) {
            LeavePugResult.CantLeft ->
                ctx.send(Emojis.NoEntry, ctx.i18nCommand("pugs.leave.cantLeft")).asEphemeral().queue()

            LeavePugResult.AlreadyLeft ->
                ctx.send(Emojis.Success, ctx.i18nCommand("pugs.leave.alreadyLeft")).asEphemeral().queue()

            LeavePugResult.Left ->
                ctx.send(Emojis.Success, ctx.i18nCommand("pugs.leave.left")).queue()

            LeavePugResult.PugDoesNotExists ->
                ctx.send(Emojis.NoEntry, notActivePug(ctx)).asEphemeral().queue()
        }
    }

    @Permissions(user = [Permission.MANAGE_SERVER])
    @SlashSubCommand("teams")
    suspend fun teams(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        val pug = Launcher.database.pugs.findByGuild(ctx.guild)
            ?: return ctx.send(Emojis.NoEntry, notActivePug(ctx)).queue()

        val players = pug.taggedPlayers

        if (players.size < 4)
            return ctx.send(
                Emojis.NoEntry, ctx.i18nCommand("pugs.teams.notEnoughPlayers", players.size)).queue()

        val redTeam = mutableListOf<String>()
        val blueTeam = mutableListOf<String>()

        for ((index, player) in players.shuffled().withIndex()) {
            if (index%2==0) redTeam.add(player)
            else blueTeam.add(player)
        }

        ctx.sendEmbed {
            setTitle(ctx.i18nCommand("pugs.teams.header"))
            addField(ctx.i18nCommand("pugs.teams.red"), redTeam.joinToString("\n"), true)
            addField(ctx.i18nCommand("pugs.teams.blue"), blueTeam.joinToString("\n"), true)
        }.queue()
    }

    @Permissions(user = [Permission.MANAGE_SERVER])
    @SlashSubCommand("create")
    suspend fun create(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        when (Launcher.database.pugs.create(ctx.guild, ctx.author)) {
            CreatePugResult.Opened ->
                ctx.send(Emojis.Success, ctx.i18nCommand("pugs.create.opened")).queue()

            CreatePugResult.CantOpen ->
                ctx.send(Emojis.NoEntry, ctx.i18nCommand("pugs.create.cantOpen")).queue()

            CreatePugResult.AlreadyActivePug ->
                ctx.send(Emojis.NoEntry, ctx.i18nCommand("pugs.create.alreadyActivePug")).queue()

        }
    }

    @Permissions(user = [Permission.MANAGE_SERVER])
    @SlashSubCommand("close")
    suspend fun close(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        when (Launcher.database.pugs.close(ctx.guild)) {
            ClosePugResult.CantClose ->
                ctx.send(Emojis.NoEntry, ctx.i18nCommand("pugs.close.cantClose")).setEphemeral(true).queue()
            ClosePugResult.Closed ->
                ctx.send(Emojis.Success, ctx.i18nCommand("pugs.close.closed")).setEphemeral(true).queue()
            ClosePugResult.NotActivePug ->
                ctx.send(Emojis.NoEntry, notActivePug(ctx)).queue()
        }
    }

    companion object {
        private fun notActivePug(ctx: SlashCommandContext) = ctx.i18nCommand("pugs.notActive")
    }

}