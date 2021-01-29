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

import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.apis.riot.entities.Region
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandProperties

@CommandProperties("top", Category.Game)
class TopCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) return ctx.reply("Specifies the game region. ${Region.values().map { it.name.toLowerCase() }}").queue()

        val region = ctx.args.first().toUpperCase()

        val players = Launcher.leaderboards[region]
            ?: return ctx.reply("` $region ` is not a valid region.").queue()

        ctx.replyEmbed {
            setTitle("Top 10 players of $region")
            setDescription("This leaderboard is from Episode 1")
            setThumbnail("https://i.imgur.com/G6wcDZB.png")
            for (player in players.take(10)) {
                addField("` Top ${player.leaderboardRank} ` ${player.fullNameTag}", buildString {
                    appendLine("**Wins**: ${player.numberOfWins}")
                    appendLine("**Rating**: ${player.rankedRating}")
                }, false)
            }
        }.queue()
    }

    override val args = listOf(
        CommandArgument(Region.values().joinToString(":") { it.name.toLowerCase() }, "Game region", true)
    )

    override val help = "Retrieve the TOP 10 players by region."
}