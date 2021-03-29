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

import tv.blademaker.killjoy.apis.news.NewsRetriever
import tv.blademaker.killjoy.slash.AbstractSlashCommand
import tv.blademaker.killjoy.slash.SlashCommandContext

@Suppress("unused")
class NewsSlashCommand : AbstractSlashCommand("news") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.acknowledge().queue()

        val latestNews = NewsRetriever.lastNews(10)

        ctx.sendEmbed {
            setTitle("Latest Valorant news")
            setDescription("This articles are from the official PlayValorant website.")

            for (new in latestNews) {
                addField(new.title, new.description+"\n[Read more](${new.url})", false)
            }

            setImage(latestNews.firstOrNull()?.image)
        }.queue()
    }

}