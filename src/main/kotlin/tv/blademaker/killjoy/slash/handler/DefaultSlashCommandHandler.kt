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

package tv.blademaker.killjoy.slash.handler

import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.slash.SlashCommandContext
import tv.blademaker.killjoy.slash.SlashUtils
import tv.blademaker.killjoy.utils.Utils

class DefaultSlashCommandHandler(packageName: String) : SlashCommandHandler {

    override val registry = SlashUtils.discoverSlashCommands(packageName)

    fun onSlashCommandEvent(event: SlashCommandEvent) = SCOPE.launch { handleSuspend(event) }

    private suspend fun handleSuspend(event: SlashCommandEvent) {
        if (event.guild == null)
            return event.reply("This command cannot be used outside of a Guild.").queue()

        val command = getCommand(event.name) ?: return
        val context = SlashCommandContext(event)

        try {
            command.execute(context)
            logCommand(event, command, LOGGER)
        } catch (e: Exception) {
            Sentry.captureException(e)
            LOGGER.error("Exception executing command ${command.commandName}.", e)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultSlashCommandHandler::class.java)
        private val SLASH_POOL = Utils.newThreadFactory("slash-pool-worker-%d", 2, 50)
        private val DISPATCHER = SLASH_POOL.asCoroutineDispatcher()
        private val PARENT_JOB = Job()
        private val SCOPE = CoroutineScope(DISPATCHER + PARENT_JOB)
    }
}