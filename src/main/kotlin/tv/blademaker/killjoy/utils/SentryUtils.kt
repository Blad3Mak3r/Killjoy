/*******************************************************************************
 * Copyright (c) 2020. Blademaker
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

package tv.blademaker.killjoy.utils

import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.abs.SubCommand

object SentryUtils {

    fun sendCommandException(ctx: CommandContext, command: Command, ex: Throwable) {
        ctx.replyError(ex).queue()

        Sentry.captureEvent(SentryEvent().apply {
            this.message = Message().apply {
                message = "Exception executing command ${command.meta.name} ${ctx.args}"
            }
            level = SentryLevel.ERROR
            setTag("Guild", "${ctx.guild.name}(${ctx.guild.id})")
            setTag("User", "${ctx.author.asTag}(${ctx.author.id})")
            throwable = ex
        })
    }

    fun sendCommandException(ctx: CommandContext, subCommand: SubCommand, ex: Throwable) {
        ctx.replyError(ex).queue()

        Sentry.captureEvent(SentryEvent().apply {
            this.message = Message().apply {
                message =
                    "Exception executing command ${subCommand.parent.meta.name} ${subCommand.meta.name} ${ctx.args}"
            }
            level = SentryLevel.ERROR
            setTag("Guild", "${ctx.guild.name}(${ctx.guild.id})")
            setTag("User", "${ctx.author.asTag}(${ctx.author.id})")
            throwable = ex
        })
    }
}