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

package tv.blademaker.slash.api.handler

import io.sentry.Sentry
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.utils.Utils
import tv.blademaker.slash.api.SlashCommandContext
import tv.blademaker.slash.utils.SlashUtils
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
            return event.reply("This command cannot be used outside of a Guild.").queue()

        val command = getCommand(event.name) ?: return
        val context = SlashCommandContext(event)

        logCommand(event, command, LOGGER)

        try {
            command.execute(context)
        } catch (e: Exception) {
            Sentry.captureException(e)
            LOGGER.error("Exception executing command ${command.commandName}.", e)

            val embed = EmbedBuilder().run {
                setColor(ColorExtra.VAL_RED)
                setAuthor("Exception executing command ${command.commandName}", null, "https://cdn.discordapp.com/emojis/690093935233990656.png")
                build()
            }

            event.hook.editOriginal(embed).setEphemeral(false).queue()
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultSlashCommandHandler::class.java)
    }
}