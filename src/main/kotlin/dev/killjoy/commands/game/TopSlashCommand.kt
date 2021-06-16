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

import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import java.time.Instant

@Suppress("unused", "DuplicatedCode")
class TopSlashCommand : AbstractSlashCommand("top") {

    override suspend fun handle(ctx: SlashCommandContext) {
        val option = ctx.getOption("region")!!.asString.uppercase()

        val region = Region.values().firstOrNull { it.name.equals(option, true) }
            ?: return ctx.sendNotFound("` $option ` is not a valid region. Valid regions: ${Region.asListed}").queue()

        ctx.event.deferReply().queue()

        val playersList = RiotAPI.LeaderboardsAPI.getCurrentTop20(region)
        val players = playersList.players.take(10)

        ctx.sendEmbed {
            setTitle("[$region] Top ${players.size} players")
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

}