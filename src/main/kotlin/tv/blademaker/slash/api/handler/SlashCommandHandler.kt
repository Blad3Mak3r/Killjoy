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

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.slf4j.Logger
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.utils.SlashUtils

interface SlashCommandHandler : EventListener {
    val registry: List<AbstractSlashCommand>

    override fun onEvent(event: GenericEvent) {
        if (event is SlashCommandEvent) onSlashCommandEvent(event)
    }

    fun onSlashCommandEvent(event: SlashCommandEvent)

    fun getCommand(name: String) = registry.firstOrNull { it.commandName.equals(name, true) }

    fun logCommand(event: SlashCommandEvent, command: AbstractSlashCommand, logger: Logger) {
        kotlin.runCatching {
            val subcommandName = event.subcommandName
                ?: event.subcommandGroup
                ?: ""
            val options = event.options.map { SlashUtils.parseOptionToString(it) }

            logger.info("[${event.guild!!.name}] ${event.user.asTag} uses command ${command.commandName} $subcommandName$options")
        }
    }
}