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

package dev.killjoy.extensions.slash

import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.utils.Emojis
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.requests.RestAction
import tv.blademaker.slash.api.SlashCommandContext
import tv.blademaker.slash.extensions.asEphemeral

fun SlashCommandContext.sendNotFound(description: String = i18n(I18nKey.CONTENT_NOT_FOUND_DESCRIPTION)): RestAction<*> {
    val embed = EmbedBuilder().run {
        setDefaultColor()
        setAuthor(i18n(I18nKey.CONTENT_NOT_FOUND), null, "https://cdn.discordapp.com/emojis/690093935233990656.png")
        setThumbnail("https://i.imgur.com/P3p4EEG.png")
        setDescription(description)
        build()
    }

    val action = if (event.isAcknowledged) hook.sendMessageEmbeds(embed) else event.replyEmbeds(embed)

    return action.asEphemeral()
}

fun SlashCommandContext.reply(emoji: Emojis, content: String) = event.reply("${emoji.getCode(this)} $content")
fun SlashCommandContext.send(emoji: Emojis, message: String) = hook.sendMessage("${emoji.getCode(this)} $message")