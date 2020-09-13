/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils

import io.sentry.Sentry
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.StackTraceInterface
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.abs.SubCommand

object SentryUtils {

    fun sendCommandException(ctx: CommandContext, command: Command, ex: Throwable) {
        ctx.replyError(ex).queue()

        Sentry.capture(EventBuilder().apply {
            withMessage("Exception executing command ${command.meta.name} ${ctx.args}")
            withLevel(Event.Level.ERROR)
            withTag("Guild", "${ctx.guild.name}(${ctx.guild.id})")
            withTag("User" ,"${ctx.author.asTag}(${ctx.author.id})")
            withSentryInterface(StackTraceInterface(ex.stackTrace))
        })
    }

    fun sendCommandException(ctx: CommandContext, subCommand: SubCommand, ex: Throwable) {
        ctx.replyError(ex).queue()

        Sentry.capture(EventBuilder().apply {
            withMessage("Exception executing command ${subCommand.parent.meta.name} ${subCommand.meta.name} ${ctx.args}")
            withLevel(Event.Level.ERROR)
            withTag("Guild", "${ctx.guild.name}(${ctx.guild.id})")
            withTag("User" ,"${ctx.author.asTag}(${ctx.author.id})")
            withSentryInterface(StackTraceInterface(ex.stackTrace))
        })
    }
}