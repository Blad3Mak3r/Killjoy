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

package dev.killjoy.slash.api.handler

import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.i18n.I18nKey
import dev.killjoy.i18n.i18n
import dev.killjoy.i18n.replyI18n
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.slash.utils.SlashUtils
import dev.killjoy.utils.Emojis
import dev.killjoy.utils.Utils
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class DefaultSlashCommandHandler(packageName: String) : SlashCommandHandler, CoroutineScope {

    private val dispatcher = Utils.newCoroutineDispatcher("slash-pool-worker-%d", 2, 50)

    override val coroutineContext: CoroutineContext
        get() = dispatcher + Job()

    override val registry = SlashUtils.discoverSlashCommands(packageName)

    override fun onSlashCommandEvent(event: SlashCommandEvent) {
        launch { handleSuspend(event) }
    }

    private suspend fun handleSuspend(event: SlashCommandEvent) {
        if (event.guild == null)
            return event.replyI18n(I18nKey.COMMAND_CANNOT_USE_OUTSIDE_GUILD).queue()

        val command = getCommand(event.name) ?: return
        val context = SlashCommandContext(event)

        logCommand(event, command, LOGGER)

        try {
            command.execute(context)
        } catch (e: Exception) {
            Sentry.captureException(e)
            LOGGER.error("Exception executing command ${command.commandName}.", e)

            val message = context.i18n(I18nKey.EXCEPTION_HANDLING_SLASH_COMMAND_OPTION, context.event.commandPath, e.message)

            if (context.event.isAcknowledged) context.send(Emojis.Cancel, message).setEphemeral(true).queue()
            else context.reply(Emojis.Cancel, message).setEphemeral(true).queue()
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultSlashCommandHandler::class.java)
    }
}