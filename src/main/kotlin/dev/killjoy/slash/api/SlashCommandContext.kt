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

package dev.killjoy.slash.api

import dev.killjoy.bot.framework.ColorExtra
import dev.killjoy.bot.utils.Emojis
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.CommandHook
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
class SlashCommandContext(
    val event: SlashCommandEvent
) {

    private val isAck = AtomicBoolean(false)

    val jda: JDA
        get() = event.jda

    @Suppress("MemberVisibilityCanBePrivate")
    val hook: CommandHook
        get() = event.hook

    val options: List<SlashCommandEvent.OptionData>
        get() = event.options

    val guild: Guild
        get() = event.guild!!

    val member: Member
        get() = event.member!!

    val selfMember: Member
        get() = event.guild!!.selfMember

    val channel: TextChannel
        get() = event.channel as TextChannel

    fun acknowledge(ephemeral: Boolean = false): CommandReplyAction {
        if (!isAck.compareAndSet(false, true)) {
            throw IllegalStateException("Current command is already ack.")
        }
        return event.acknowledge(ephemeral)
    }

    fun sendNotFound(description: String = "I couldn't find what you were looking for."): RestAction<*> {
        val embed = EmbedBuilder().run {
            setColor(ColorExtra.VAL_RED)
            setAuthor("Content not found", null, "https://cdn.discordapp.com/emojis/690093935233990656.png")
            setThumbnail("https://i.imgur.com/P3p4EEG.png")
            setDescription(description)
            build()
        }

        return if (event.isAcknowledged) hook.sendMessage(embed)
        else event.reply(embed)
    }

    fun getOption(name: String) = event.getOption(name)

    fun reply(content: String) = event.reply(content)

    fun reply(embed: MessageEmbed) = event.reply(embed)

    fun send(content: String) = hook.sendMessage(content)

    fun send(emoji: Emojis, message: String) = hook.sendMessage("${emoji.getCode(this)} $message")

    fun send(embed: MessageEmbed) = hook.sendMessage(embed)

    fun send(embedBuilder: EmbedBuilder) = hook.sendMessage(embedBuilder.build())

    fun sendEmbed(builder: EmbedBuilder.() -> Unit): InteractionWebhookAction {
        val embed = EmbedBuilder()
            .setColor(ColorExtra.VAL_RED)
            .apply(builder).build()

        return hook.sendMessage(embed)
    }
}