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

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Command
import tv.blademaker.killjoy.apis.riot.RiotAPI
import tv.blademaker.killjoy.apis.riot.entities.Region
import tv.blademaker.killjoy.slash.AbstractSlashCommand
import tv.blademaker.killjoy.slash.SlashCommandContext
import tv.blademaker.killjoy.slash.SlashCommandOption

@Suppress("unused", "DuplicatedCode")
class TopSlashCommand : AbstractSlashCommand("top") {

    @SlashCommandOption(Command.OptionType.STRING)
    suspend fun region(ctx: SlashCommandContext) {
        val option = ctx.getOption("region")!!.asString.toUpperCase()

        val region = Region.values().firstOrNull { it.name.equals(option, true) }
            ?: return ctx.event.reply("` $option ` is not a valid region. Valid regions: ${Region.values().joinToString(", ") { "**${it.name}**"}}").queue()

        ctx.event.acknowledge().queue()

        val players = RiotAPI.getCurrentTop20(region)

        val embed = EmbedBuilder()
            .apply {
                setTitle("Current Top 10 players of $region")
                setThumbnail("https://i.imgur.com/G6wcDZB.png")
                for (player in players.take(10)) {
                    addField("` Top ${player.leaderboardRank} ` ${player.fullNameTag}", buildString {
                        appendLine("**Wins**: ${player.numberOfWins}")
                        appendLine("**Rating**: ${player.rankedRating}")
                    }, false)
                }
            }
            .build()

        ctx.hook.sendMessage(embed).queue()
    }

}