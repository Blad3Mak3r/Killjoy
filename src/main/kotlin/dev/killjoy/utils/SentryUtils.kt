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
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import io.sentry.protocol.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.SlashCommandContext
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

    fun captureSlashCommandException(ctx: SlashCommandContext, e: Throwable, logger: Logger? = null) {
        val message = ctx.i18n(I18nKey.EXCEPTION_HANDLING_SLASH_COMMAND_OPTION, ctx.event.commandPath, e.message)

        if (ctx.event.isAcknowledged) ctx.send(Emojis.Outage, message).setEphemeral(true).queue()
        else ctx.reply(Emojis.Outage, message).setEphemeral(true).queue()

        val errorMessage = "Exception executing handler for ${ctx.event.commandPath}, ${e.message}"

        fun getUserType(): String {
            val isSystem = ctx.author.isSystem
            val isBot = ctx.author.isBot

            return when {
                isSystem -> "System"
                isBot -> "Bot"
                else -> "User"
            }
        }

        Sentry.captureEvent(SentryEvent().apply {
            this.message = Message().apply {
                this.message = errorMessage
            }
            this.user = User().apply {
                this.id = ctx.author.id
                this.username = ctx.author.asTag
                this.others = mapOf("type" to getUserType())
            }
            this.setExtra("Guild", "${ctx.guild.name} (${ctx.guild.id})")
            this.setExtra("Command Path", ctx.event.commandPath)
            throwable = e
        })

        logger?.error(errorMessage, e)
    }
}