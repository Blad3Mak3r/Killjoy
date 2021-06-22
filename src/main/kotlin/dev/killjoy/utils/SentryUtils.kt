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

package dev.killjoy.utils

import dev.killjoy.Credentials
import dev.killjoy.Versions
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.abs.SubCommand
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException

object SentryUtils {

    private val LOGGER = LoggerFactory.getLogger(SentryUtils::class.java)

    private val serverName: String?
        get() {
            return try {
                InetAddress.getLocalHost().hostName
            } catch (e: UnknownHostException) {
                null
            }
        }

    fun init() {
        val dsn = Credentials.getOrNull<String>("sentry.dsn")
            ?: return

        LOGGER.info("Initializing sentry with DSN $dsn")
        Sentry.init { options ->
            options.environment = "production"
            options.dsn = dsn
            options.release = Versions.KILLJOY
            options.setDebug(false)
            serverName?.let { sn -> options.serverName = sn }
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