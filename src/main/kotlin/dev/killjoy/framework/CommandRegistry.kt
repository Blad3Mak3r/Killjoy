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

package dev.killjoy.framework

import dev.killjoy.Launcher
import dev.killjoy.commands.game.*
import dev.killjoy.commands.info.HelpCommand
import dev.killjoy.commands.info.InviteCommand
import dev.killjoy.commands.info.PingCommand
import dev.killjoy.commands.misc.MemeCommand
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.abs.SubCommand
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.framework.annotations.SubCommandProperties
import dev.killjoy.utils.Emojis
import dev.killjoy.utils.SentryUtils
import dev.killjoy.utils.Utils
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.LoggerFactory
import java.lang.annotation.IncompleteAnnotationException

class CommandRegistry {

    private val executor = Utils.newThreadFactory("command-pool-worker-%d", 6, 20)
    private val dispatcher = executor.asCoroutineDispatcher()
    private val commandsScope = CoroutineScope(dispatcher)

    private val commands: HashMap<String, Command> = HashMap()

    init {
        addCommands(
            //Information commands
            HelpCommand(),
            InviteCommand(),
            NewsCommand(),
            PingCommand(),

            //Game commands
            AgentCommand(),
            ArsenalCommand(),
            MapsCommand(),
            SkillCommand(),
            TopCommand(),

            //Misc
            MemeCommand(),
        )
    }

    fun shutdown() {
        executor.shutdown()
    }

    fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val user = event.author
        if (user.idLong == event.guild.selfMember.user.idLong || user.isBot || event.isWebhookMessage)
            return

        if (Launcher.rateLimiter.isExceeded(user.idLong)) return

        val mention = "<@!${event.jda.selfUser.id}> "
        val raw = event.message.contentRaw.trim()
        val prefix = "joy "

        when {
            raw.startsWith(prefix) -> commandsScope.launch { analyzeMessage(event, prefix) }
            raw.startsWith(mention) -> commandsScope.launch { analyzeMessage(event, mention) }
        }
    }

    private fun addCommands(vararg commands: Command) {
        for (command in commands) {
            addCommand(command)
        }
    }

    private fun addCommand(command: Command) {
        if (!command::class.java.isAnnotationPresent(CommandProperties::class.java))
            throw IncompleteAnnotationException(CommandProperties::class.java, command::class.java.name)

        if (commands.containsKey(command.props.name))
            throw IllegalArgumentException("A command with the name ${command.props.name} is already present.")

        for (subCommand in command.getSubCommands()) {
            if (!subCommand.javaClass.isAnnotationPresent(SubCommandProperties::class.java))
                throw IncompleteAnnotationException(SubCommandProperties::class.java, subCommand.javaClass.name)
        }
        commands[command.props.name] = command
    }

    fun getCommands(category: Category? = null): List<Command> {
        return if (category == null) commands.values.filter { it.props.category.isPublic && it.props.category.isEnabled }
        else commands.values.filter { it.props.category == category }
    }

    fun getCommand(invoke: String, admin: Boolean = false): Command? {
        val search = invoke.lowercase()
        val cmd = commands[search] ?: commands.values.find { it.props.aliases.contains(search) } ?: return null

        return when {
            admin || cmd.props.category != Category.Owner -> cmd
            !admin || cmd.props.category == Category.Owner -> null
            else -> cmd
        }
    }

    private suspend fun analyzeMessage(event: GuildMessageReceivedEvent, prefix: String) {
        val channel = event.channel
        val bot = event.guild.selfMember

        if (!bot.hasPermission(channel, Permission.MESSAGE_WRITE))
            return

        val split = event.message.contentRaw
                .replaceFirst(prefix, "")
                .split(" ")

        val invoke = split[0].lowercase()

        val command = getCommand(invoke) ?: return

        if (!command.props.category.isEnabled) return


        handleCommand(CommandContext(event, split.subList(1, split.size)), command)
    }

    private suspend fun handleCommand(context: CommandContext, command: Command) {
        log.info("[${context.guild.name}] ${context.author.name} used \"${command.props.name} ${context.args}\" command in channel ${context.channel.name}")

        val channel = context.channel
        val member = context.member

        if (Launcher.rateLimiter.isRateLimited(member.idLong))
            return context.reply(Emojis.Outage, "You are getting rate limited.").queue()

        val cdBucket = Launcher.cooldownManager.check(context, command)
        if (cdBucket != null && !cdBucket.announced) return context.reply("This command is in cooldown... you have to wait **${cdBucket.restInSeconds}** seconds.").queue()
        else if (cdBucket != null && cdBucket.announced) return

        val userPermissions = command.userPermissions.filter { !it.isVoice }.toSet()
        val botPermissions = command.botPermissions.filter { !it.isVoice }.toSet()

        if (!member.hasPermission(channel, userPermissions))
            return context.send("""
                    you do not have the necessary permissions to perform this action... ${userPermissions.joinToString(", ") { "***${it.getName()}***" }}
                """.trimIndent()).queue()

        if (!context.selfMember.hasPermission(channel, botPermissions))
            return context.send("""
                    I do not have the necessary permissions to perform this action... ${botPermissions.joinToString(", ") { "***${it.getName()}***" }}
                """.trimIndent()).queue()

        try {
            handleExecutor(context, command)
        } catch (ex: InsufficientPermissionException) {
            val msg = String.format("Cannot perform action due to a lack of Permission. **Missing permission: %s**", ex.permission.getName())
            context.send(msg).queue()
            Sentry.captureException(ex)
        } catch (ex: Throwable) {
            SentryUtils.sendCommandException(context, command, ex)
        }
    }

    private suspend fun handleExecutor(context: CommandContext, command: Command) {
        if (context.args.isNotEmpty()) {
            val invoke = context.args[0]
            val subCommands = command.getSubCommands()
            val subCommand = Utils.Commands.getSubCommand(invoke, subCommands)
            val hasSubCommands = subCommands.isNotEmpty()
            val hasArgs = command.args.isNotEmpty()
            val isSubCommand = subCommand != null

            when {
                hasSubCommands && hasArgs && isSubCommand -> runSubCommand(context, subCommand!!)
                hasSubCommands && hasArgs -> runCommand(context, command)
                hasSubCommands && isSubCommand -> runSubCommand(context, subCommand!!)
                hasSubCommands -> Utils.Commands.replyWrongUsage(context, command)
                else -> runCommand(context, command)
            }

        } else runCommand(context, command)
    }

    private suspend fun runCommand(context: CommandContext, command: Command) {
        if (command.props.isNsfw && !context.channel.isNSFW)
            context.send(Emojis.Nsfw, "You cannot use this command on a **non-NSFW** channel.").queue()
        else {
            command.execute(context)
        }
    }

    private suspend fun runSubCommand(context: CommandContext, command: SubCommand) {
        if (command.props.isNsfw && !context.channel.isNSFW)
            context.send(Emojis.Nsfw, "You cannot use this command on a **non-NSFW** channel.").queue()
        else {
            context.args = context.args.subList(1, context.args.size)
            command.execute(context)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CommandRegistry::class.java)
    }
}