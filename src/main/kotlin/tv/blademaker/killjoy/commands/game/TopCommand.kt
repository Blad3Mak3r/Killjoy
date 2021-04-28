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

package tv.blademaker.killjoy.commands.game

import tv.blademaker.killjoy.apis.riot.RiotAPI
import tv.blademaker.killjoy.apis.riot.entities.Region
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandArgument
import tv.blademaker.killjoy.framework.annotations.CommandProperties
import tv.blademaker.killjoy.utils.Emojis
import java.time.Instant

@CommandProperties(
    name = "top",
    category = Category.Game,
    arguments = [
        CommandArgument("AP, BR, EU, KR, LATAM, NA", "Game region", true)
    ])
class TopCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) return ctx.reply("Specifies the game region. ${Region.values().map { it.name.toLowerCase() }}").queue()

        val arg = ctx.args.first().toUpperCase()

        val region = Region.values().firstOrNull { it.name.equals(arg, true) }
            ?: return ctx.reply(Emojis.NoEntry, "` $arg ` is not a valid region. Valid regions: ${Region.values().joinToString(", ") { "**${it.name}**"}}").queue()

        val playersList = RiotAPI.LeaderboardsAPI.getCurrentTop20(region)
        val players = playersList.players.take(10)

        ctx.replyEmbed {
            setTitle("Current Top ${players.size} players of $region)")
            setThumbnail("https://i.imgur.com/G6wcDZB.png")
            for (player in players) {
                addField("` Top ${player.leaderboardRank} ` ${player.fullNameTag}", buildString {
                    appendLine("**Wins**: ${player.numberOfWins}")
                    appendLine("**Rating**: ${player.rankedRating}")
                }, false)
            }
            setTimestamp(Instant.ofEpochMilli(playersList.updatedAt))
            setFooter("Episode 2 Act 3 | Updated at")
        }.queue()
    }

    override val help = "Retrieve the TOP 10 players by region."
}