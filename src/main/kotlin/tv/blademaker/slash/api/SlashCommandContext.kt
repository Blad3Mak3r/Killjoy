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

package tv.blademaker.slash.api

import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import tv.blademaker.slash.utils.SlashUtils.asEphemeral
import dev.killjoy.utils.Emojis
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction

@Suppress("unused")
class SlashCommandContext(
    val event: SlashCommandEvent
) {

    val isAcknowledged: Boolean
        get() = event.isAcknowledged

    val jda: JDA
        get() = event.jda

    @Suppress("MemberVisibilityCanBePrivate")
    val hook: InteractionHook
        get() = event.hook

    val options: List<OptionMapping>
        get() = event.options

    val guild: Guild
        get() = event.guild!!

    val member: Member
        get() = event.member!!

    val selfMember: Member
        get() = event.guild!!.selfMember

    val channel: TextChannel
        get() = event.channel as TextChannel

    val author: User
        get() = event.user

    fun acknowledge(ephemeral: Boolean = false): ReplyAction {
        if (isAcknowledged) throw IllegalStateException("Current command is already ack.")
        return event.deferReply(ephemeral)
    }

    fun sendNotFound(description: String = i18n(I18nKey.CONTENT_NOT_FOUND_DESCRIPTION)): RestAction<*> {
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

    fun getOption(name: String) = event.getOption(name)

    fun reply(content: String) = event.reply(content)

    fun reply(emoji: Emojis, content: String) = event.reply("${emoji.getCode(this)} $content")

    fun reply(embed: MessageEmbed) = event.replyEmbeds(embed)

    fun replyMessage(builder: MessageBuilder.() -> Unit): ReplyAction {
        val message = MessageBuilder().apply(builder).build()

        return event.reply(message)
    }

    fun replyEmbed(builder: EmbedBuilder.() -> Unit): ReplyAction {
        val embed = EmbedBuilder()
            .setDefaultColor()
            .apply(builder).build()

        return event.replyEmbeds(embed)
    }

    fun send(content: String) = hook.sendMessage(content)

    fun send(emoji: Emojis, message: String) = hook.sendMessage("${emoji.getCode(this)} $message")

    fun send(embed: MessageEmbed) = hook.sendMessageEmbeds(embed)

    fun send(embedBuilder: EmbedBuilder) = hook.sendMessageEmbeds(embedBuilder.build())

    fun sendEmbed(builder: EmbedBuilder.() -> Unit): WebhookMessageAction<Message> {
        val embed = EmbedBuilder()
            .setDefaultColor()
            .apply(builder).build()

        return hook.sendMessageEmbeds(embed)
    }
}