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

package dev.killjoy.bot.utils

import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import org.slf4j.LoggerFactory
import dev.killjoy.bot.BotConfig
import dev.killjoy.bot.framework.CommandContext
import dev.killjoy.bot.framework.abs.Command
import dev.killjoy.bot.framework.abs.SubCommand

object SentryUtils {

    private val LOGGER = LoggerFactory.getLogger(SentryUtils::class.java)

    fun init() {
        val dsn = BotConfig.getOrNull<String>("sentry.dsn")
            ?: return

        LOGGER.info("Initializing sentry with DSN $dsn")
        Sentry.init { options ->
            options.dsn = dsn
        }
    }

    fun sendCommandException(ctx: CommandContext, command: Command, ex: Throwable) {
        ctx.replyError(ex).queue()

        Sentry.captureEvent(SentryEvent().apply {
            this.message = Message().apply {
                message = "Exception executing command ${command.props.name} ${ctx.args}"
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
                    "Exception executing command ${subCommand.parent.props.name} ${subCommand.props.name} ${ctx.args}"
            }
            level = SentryLevel.ERROR
            setTag("Guild", "${ctx.guild.name}(${ctx.guild.id})")
            setTag("User", "${ctx.author.asTag}(${ctx.author.id})")
            throwable = ex
        })
    }
}