/*
 * Copyright (c) 2020.
 * BladeMaker
 */
package tv.blademaker.killjoy.utils

import net.dv8tion.jda.api.Permission
import tv.blademaker.killjoy.framework.CommandContext

enum class Emojis(private val custom: String?, private val def: String) {
    WasteBasket(null, "\uD83D\uDDD1️"),
    BangBang(null, "‼️"),
    Cancel(null, "❌"),
    Loading("<a:loading:689343143765868590>", "\uD83C\uDF00"),
    Typing("<a:typing:689341473136836615>", "✏️"),
    Success("<a:success:689347416918458370>", "✅"),
    BlobbleWobble("<a:bw:689350317296320513>", "\uD83E\uDD24"),
    Outage("<:outage:690093935233990656>", "❗"),
    PepoDance("<a:pepodance:713731405321863211>", "\uD83C\uDF9A️"),
    Mag(null, "\uD83D\uDD0D"),
    Nsfw(null, "\uD83D\uDD1E"),
    NoEntry(null, "\uD83D\uDEAB"),
    Stop(null, "\uD83D\uDED1"),
    StopWatch(null, "⏱️"),
    Muted(null, "\uD83D\uDD07"),
    Thinking(null, "🤔"),
    Vote(null, "\uD83D\uDCDD"),
    Forward(null, "⏩"),
    Rewind(null, "⏪"),
    RepeatOne(null, "🔂"),
    RepeatAll(null, "\uD83D\uDD01"),
    ArrowRight(null, "➡️");

    fun getCode(): String {
        return custom ?: def
    }

    fun getCode(context: CommandContext): String {
        return if (context.selfMember.hasPermission(context.channel, Permission.MESSAGE_EXT_EMOJI)) getCode()
        else def
    }

    fun getSnowflake(): String {
        return custom?.replace("<", "")?.replace(">", "")
                ?: def.replace("<", "").replace(">", "")
    }

    fun getSnowflake(context: CommandContext): String {
        return if (context.selfMember.hasPermission(context.channel, Permission.MESSAGE_EXT_EMOJI)) getSnowflake()
        else def.replace("<", "").replace(">", "")
    }
}