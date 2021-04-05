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

package tv.blademaker.slash.api

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.annotations.Permissions
import tv.blademaker.slash.api.annotations.SlashSubCommand
import tv.blademaker.slash.utils.SlashUtils
import java.util.function.Predicate
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

abstract class AbstractSlashCommand(val commandName: String) {

    val checks: MutableList<Predicate<SlashCommandContext>> = mutableListOf()

    private val subCommands: List<SubCommand> = this::class.functions
        .filter { it.hasAnnotation<SlashSubCommand>() && it.visibility == KVisibility.PUBLIC && !it.isAbstract }
        .map { SubCommand(it) }

    private fun doChecks(ctx: SlashCommandContext): Boolean {
        if (checks.isEmpty()) return true
        return checks.all { it.test(ctx) }
    }

    private suspend fun handleSubcommand(ctx: SlashCommandContext): Boolean {
        val subCommandGroup = ctx.event.subcommandGroup

        val subCommandName = ctx.event.subcommandName
            ?: return false

        try {
            val subCommand = subCommands
                .filter { if (subCommandGroup != null) it.groupName == subCommandGroup else true }
                .find { s -> s.name.equals(subCommandName, true) }

            if (subCommand == null) {
                LOGGER.warn("Not found any valid handle for option \"$subCommandName\", executing default handler.")
                return false
            }

            LOGGER.debug("Executing \"${subCommand.name}\" for option \"$subCommandName\"")

            try {
                if (!SlashUtils.hasPermissions(ctx, subCommand.permissions)) return true

                subCommand.execute(this, ctx)
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
        if (handleSubcommand(ctx)) return

        handle(ctx)
    }

    internal open suspend fun handle(ctx: SlashCommandContext) {
        ctx.event.reply("Slash command not yeet implemented by developer.").queue()
    }

    class SubCommand private constructor(
        private val handler: KFunction<*>,
        private val annotation: SlashSubCommand,
        val permissions: Permissions?
    ) {

        constructor(f: KFunction<*>) : this(
            handler = f,
            annotation = f.findAnnotation<SlashSubCommand>()!!,
            permissions = f.findAnnotation<Permissions>()
        )

        val name: String
            get() = annotation.name.takeIf { it.isNotBlank() } ?: handler.name

        val groupName: String
            get() = annotation.group

        suspend fun execute(instance: AbstractSlashCommand, ctx: SlashCommandContext) = handler.callSuspend(instance, ctx)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractSlashCommand::class.java)
    }
}