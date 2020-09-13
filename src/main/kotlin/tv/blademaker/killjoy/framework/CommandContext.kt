/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.framework

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.sharding.ShardManager
import tv.blademaker.killjoy.utils.extensions.sendMessage
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.utils.Emojis
import java.awt.Color

@Suppress("ReplaceGetOrSet", "unused")
class CommandContext(
        val event: GuildMessageReceivedEvent,
        split: List<String>
) {

    var args: List<String> = split

    val jda: JDA
        get() = event.jda

    val shardManager: ShardManager?
        get() = jda.shardManager

    val guild: Guild
        get() = event.guild

    val channel: TextChannel
        get() = event.channel

    val message: Message
        get() = event.message

    val author: User
        get() = event.author

    val member: Member
        get() = event.member!!

    val selfUser: User
        get() = jda.selfUser

    val selfMember: Member
        get() = guild.selfMember

    val isSelf: Boolean
        get() = author.id == selfUser.id

    fun updateArgs(args: List<String>): CommandContext {
        this.args = args
        return this
    }

    fun reply(message: String) = channel.sendMessage {
        append(author.asMention)
        append(", ")
        append(message)
    }

    fun reply(embed: MessageEmbed): MessageAction {
        return channel.sendMessage {
            append(author.asMention)
            setEmbed(embed)
        }
    }

    fun reply(emoji: Emojis, msg: String): MessageAction {
        return channel.sendMessage {
            append(emoji.getCode(this@CommandContext))
            append(" ")
            append(author.asMention)
            append(", ")
            append(msg)
        }
    }

    fun reply(emoji: Emojis, embed: MessageEmbed): MessageAction {
        return channel.sendMessage {
            append(emoji.getCode(this@CommandContext))
            append(" ")
            setEmbed(embed)
        }
    }

    fun replyError(error: Throwable): MessageAction {
        return send {
            append(Emojis.NoEntry.getCode(this@CommandContext))
            append(" **")
            append(error::class.java.simpleName)
            append("**\n")
            appendCodeBlock("[This exception is automatically reported to developers]\n\n${error.localizedMessage}", "yaml")
        }
    }

    fun replyError(error: String): MessageAction {
        val embed: MessageEmbed = EmbedBuilder()
                .setAuthor("An unexpected exception occurred.")
                .setDescription("```\n$error\n```").setColor(Color.RED).build()

        return channel.sendMessage {
            append(author.asMention)
            setEmbed(embed)
        }
    }

    fun send(emoji: Emojis, msg: String): MessageAction {
        return channel.sendMessage {
            append(emoji.getCode(this@CommandContext))
            append(" ")
            append(msg)
        }
    }

    fun send(msg: String) = channel.sendMessage(msg)

    fun send(msg: Message) = channel.sendMessage(msg)

    fun send(embed: MessageEmbed) = channel.sendMessage(embed)

    inline fun send(builder: MessageBuilder.() -> Unit) = channel.sendMessage { apply(builder) }

    fun embed(title: String?, description: String): MessageAction {
        return embed {
            setTitle(title)
            setDescription(description)
        }
    }

    fun getEmbedColor(member: Member): Color {
        return member.color ?: ColorExtra.VAL_RED
    }

    inline fun embed(embed: EmbedBuilder.() -> Unit) =
            channel.sendMessage(EmbedBuilder()
                    .setColor(getEmbedColor(selfMember))
                    .apply(embed)
                    .build())

    fun sendError(error: String?) = channel.sendMessage {
        appendCodeBlock(error ?: "Unknown error received", "css")
    }

    fun reactDone(): RestAction<Void> {
        return event.message.addReaction("✌")
    }

    fun react(emote: Emote): RestAction<Void> {
        return event.message.addReaction(emote)
    }

    fun react(emote: String): RestAction<Void> {
        return event.message.addReaction(emote)
    }

    fun reactFail(): RestAction<Void> {
        return event.message.addReaction("❌")
    }
}