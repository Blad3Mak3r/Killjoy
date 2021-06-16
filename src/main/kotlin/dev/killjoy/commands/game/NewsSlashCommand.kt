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

import dev.killjoy.apis.news.NewsRetriever
import dev.killjoy.framework.ColorExtra
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import net.dv8tion.jda.api.EmbedBuilder

@Suppress("unused")
class NewsSlashCommand : AbstractSlashCommand("news") {

    override suspend fun handle(ctx: SlashCommandContext) {
        val isCached = NewsRetriever.cached

        if (!isCached) ctx.acknowledge().queue()

        val latestNews = NewsRetriever.lastNews(10)

        val embed = EmbedBuilder().run {
            setColor(ColorExtra.VAL_RED)
            setTitle("Latest Valorant news")
            setDescription("This articles are from the official PlayValorant website.")
            for (new in latestNews) {
                addField(new.title, new.description+"\n[Read more](${new.url})", false)
            }
            setImage(latestNews.firstOrNull()?.image)
            build()
        }

        if (!isCached) ctx.send(embed).queue()
        else ctx.reply(embed).queue()
    }

}