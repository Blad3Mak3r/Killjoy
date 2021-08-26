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
import dev.killjoy.apis.news.NewsRetriever
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.i18n.i18nCommand

@CommandProperties(
    name = "news",
    category = Category.Game
)
class NewsCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        val latestNews = Launcher.cache.news.get(ctx.guild.supportedLocale)

        ctx.replyEmbed {
            setTitle(ctx.guild.i18nCommand("news.header"))
            setDescription(ctx.guild.i18nCommand("news.info"))

            for (new in latestNews) {
                addField(new.asEmbedField(ctx.guild))
            }

            setImage(latestNews.firstOrNull()?.image)
        }.queue()
    }

    override val help = "Retrieve the latest news from the official PlayValorant website."
}