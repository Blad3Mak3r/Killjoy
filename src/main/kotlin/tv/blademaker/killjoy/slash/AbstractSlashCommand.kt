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

package tv.blademaker.killjoy.slash

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import java.util.function.Predicate
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

abstract class AbstractSlashCommand(val commandName: String) {

    val checks: MutableList<Predicate<SlashCommandContext>> = mutableListOf()

    private fun doChecks(ctx: SlashCommandContext): Boolean {
        if (checks.isEmpty()) return true
        return checks.all { it.test(ctx) }
    }

    private suspend fun handleCommandOption(ctx: SlashCommandContext): Boolean {
        val subCommandName = ctx.event.subcommandName
            ?: ctx.options.firstOrNull()?.name
            ?: return false

        try {
            val function = this::class.functions.filter { it.hasAnnotation<SlashCommandOption>() }.find { func ->
                func.name.equals(subCommandName, true)
            }

            if (function == null) {
                LOGGER.warn("Not found any valid handle for option \"$subCommandName\", executing default handler.")
                return false
            }

            LOGGER.debug("Executing \"${function.name}\" for option \"$subCommandName\"")

            val annotation = function.findAnnotation<SlashCommandOption>()!!

            try {
                function.callSuspend(this, ctx)
            } catch (e: Exception) {
                LOGGER.error("Exception executing handler for option $subCommandName")
                return true
            }
            return true
        } catch (e: Exception) {
            LOGGER.error("Exception getting KFunctions to handle subcommand $subCommandName", e)
            Sentry.captureException(e)
            return false
        }
    }

    open suspend fun execute(ctx: SlashCommandContext) {
        if (!doChecks(ctx)) return
        if (handleCommandOption(ctx)) return

        handle(ctx)
    }

    internal open suspend fun handle(ctx: SlashCommandContext) {
        ctx.event.reply("Slash command not yeet implemented by developer.").queue()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractSlashCommand::class.java)
    }
}