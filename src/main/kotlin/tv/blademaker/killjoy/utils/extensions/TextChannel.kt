/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.TextChannel
import tv.blademaker.killjoy.utils.Emojis

inline fun TextChannel.sendEmbed(builder: EmbedBuilder.() -> Unit) = this.sendMessage(EmbedBuilder().apply(builder).build())
inline fun TextChannel.sendMessage(builder: MessageBuilder.() -> Unit) = this.sendMessage(MessageBuilder().apply(builder).build())

fun TextChannel.sendMessage(emojis: Emojis, message: CharSequence) = this.sendMessage("${emojis.getCode()} $message")