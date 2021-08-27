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

package dev.killjoy.commands.game

import dev.killjoy.Launcher
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandArgument
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.i18n.i18nCommand
import dev.killjoy.utils.Emojis
import java.time.Instant

@CommandProperties(
    name = "top",
    category = Category.Game,
    arguments = [
        CommandArgument("AP, BR, EU, KR, LATAM, NA", "Game region", true)
    ])
class TopCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) return ctx.reply("Specifies the game region. ${Region.asListed}").queue()

        val arg = ctx.args.first().uppercase()

        val region = Region.values().firstOrNull { it.name.equals(arg, true) }
            ?: return ctx.reply(Emojis.NoEntry, ctx.guild.i18nCommand("top.notValidRegion", arg, Region.asListed)).queue()

        val playersList = Launcher.getLeaderboard(region)
        val players = playersList.players.take(10)

        ctx.replyEmbed {
            setTitle(ctx.guild.i18nCommand("top.header", region, players.size))
            setThumbnail("https://i.imgur.com/G6wcDZB.png")
            for (player in players) {
                val content = ctx.guild.i18nCommand("top.content", player.rankedRating, player.numberOfWins)
                addField("` ${player.leaderboardRank} ` ${player.fullNameTag}", content, false)
            }
            setTimestamp(Instant.ofEpochMilli(playersList.updatedAt))
            setFooter(ctx.guild.i18nCommand("top.footer", 3, 1))
        }.queue()
    }

    override val help = "Retrieve the TOP 10 players by region."
}