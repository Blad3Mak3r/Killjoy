/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.framework

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.sentry.Sentry
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.StackTraceInterface
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.abs.SubCommand
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import tv.blademaker.killjoy.framework.annotations.SubCommandMeta
import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.commands.AgentCommand
import tv.blademaker.killjoy.commands.HelpCommand
import tv.blademaker.killjoy.commands.SkillCommand
import tv.blademaker.killjoy.utils.Emojis
import tv.blademaker.killjoy.utils.SentryUtils
import tv.blademaker.killjoy.utils.Utils
import java.lang.annotation.IncompleteAnnotationException
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

class CommandRegistry : ListenerAdapter() {

    private val executor = Executors.newCachedThreadPool(
            ThreadFactoryBuilder().setNameFormat("CommandExecutor #%d").build())

    private val commands: HashMap<String, Command> = HashMap()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Shutting down...")
            executor.shutdown()
        })
        addCommands(
                AgentCommand(),
                HelpCommand(),
                SkillCommand()
        )
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val user = event.author
        if (user.idLong == event.guild.selfMember.user.idLong || user.isBot || event.isWebhookMessage)
            return

        if (Launcher.rateLimiter.isExceeded(user.idLong)) return

        try {

            val mention = "<@!${event.jda.selfUser.id}> "
            val raw = event.message.contentRaw.trim()
            val prefix = "joy "


            when {
                raw.startsWith(prefix) -> executor.execute { analiceMessage(event, prefix) }
                raw.startsWith(mention) -> executor.execute { analiceMessage(event, mention) }
            }

        } catch (ex: Exception) {
            Sentry.capture(EventBuilder()
                    .withMessage("Cannot get config for Guild " + event.guild.name + " (" + event.guild.id + ")")
                    .withLevel(Event.Level.ERROR)
                    .withTag("Guild", "${event.guild.name}(${event.guild.id})")
                    .withTag("Text Channel", "${event.channel.name}(${event.channel.id})")
                    .withTag("Author", "${event.author.name}(${event.author.id})")
                    .withSentryInterface(StackTraceInterface(ex.stackTrace))
                    .withTimestamp(Date.from(Instant.now())))

            ex.printStackTrace()

            log.error("No se ha podido obtener información o crear el Guild " + event.guild.id + "\n" + ex)
        }
    }

    fun getHits(): Int {
        val hits = AtomicInteger(commands.map { it.value.getHits() }.reduce { acc, i -> acc + i })

        for (command in commands.values) {
            val subCommands = command.subCommands

            if (subCommands.isEmpty()) continue

            for (subCommand in subCommands) {
                hits.addAndGet(subCommand.hits.get())
            }
        }

        return hits.get()
    }

    private fun addCommands(vararg commands: Command) {
        for (command in commands) {
            addCommand(command)
        }
    }

    private fun addCommand(command: Command) {
        if (!command::class.java.isAnnotationPresent(CommandMeta::class.java))
            throw IncompleteAnnotationException(CommandMeta::class.java, command::class.java.name)

        if (commands.containsKey(command.meta.name))
            throw IllegalArgumentException("A command with the name ${command.meta.name} is already present.")

        for (subCommand in command.subCommands) {
            if (!subCommand.javaClass.isAnnotationPresent(SubCommandMeta::class.java))
                throw IncompleteAnnotationException(SubCommandMeta::class.java, subCommand.javaClass.name)
        }
        commands[command.meta.name] = command
    }

    fun getCommands(category: Category? = null): List<Command> {
        return if (category == null) commands.values.filter { it.meta.category.isPublic && it.meta.category.isEnabled }
        else commands.values.filter { it.meta.category == category }
    }

    fun getCommand(invoke: String, admin: Boolean = false): Command? {
        val search = invoke.toLowerCase()
        val cmd = commands[search] ?: commands.values.find { it.meta.aliases.contains(search) } ?: return null

        return when {
            admin || cmd.meta.category != Category.Owner -> cmd
            !admin || cmd.meta.category == Category.Owner -> null
            else -> cmd
        }
    }

    private fun analiceMessage(event: GuildMessageReceivedEvent, prefix: String) {
        val channel = event.channel
        val author = event.author
        val bot = event.guild.selfMember

        if (!bot.hasPermission(channel, Permission.MESSAGE_WRITE))
            return

        val split = event.message.contentRaw
                .replaceFirst(prefix, "")
                .split(" ")

        val invoke = split[0].toLowerCase()
        // val isAdmin = huge.configuration.owners.contains(author.idLong)
        // val isAdmin = Config.owners.any { it == author.id }

        val command = getCommand(invoke) ?: return

        if (!command.meta.category.isEnabled) return


        handleCommand(CommandContext(event, split.subList(1, split.size)), command)
    }

    private fun handleCommand(context: CommandContext, command: Command) {
        log.info("[${context.guild.name}] ${context.author.name} used \"${command.meta.name} ${context.args}\" command in channel ${context.channel.name}")

        val channel = context.channel
        val member = context.member

        if (Launcher.rateLimiter.isRateLimited(member.idLong))
            return context.reply(Emojis.Outage, "You are getting rate limited.").queue()

        /*if (huge.cooldownUtils.inCooldown(context, command))
            return context.message.addReaction(EMOJI_COOLDOWN).queue()*/

        val userPermissions = command.userPermissions.filter { !it.isVoice }.toSet()
        val botPermissions = command.botPermissions.filter { !it.isVoice }.toSet()

        if (!member.hasPermission(channel, userPermissions))
            return context.reply("""
                    you do not have the necessary permissions to perform this action... ${userPermissions.joinToString(", ") { "***${it.getName()}***" }}
                """.trimIndent()).queue()

        if (!context.selfMember.hasPermission(channel, botPermissions))
            return context.reply("""
                    I do not have the necessary permissions to perform this action... ${botPermissions.joinToString(", ") { "***${it.getName()}***" }}
                """.trimIndent()).queue()

        handleExecutor(context, command)
    }

    private fun handleExecutor(context: CommandContext, command: Command) {
        if (context.args.isNotEmpty()) {
            val invoke = context.args[0]
            val subCommands = command.subCommands
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

    private fun runCommand(context: CommandContext, command: Command) {
        if (command.meta.isNsfw && !context.channel.isNSFW)
            context.send(Emojis.Nsfw, "You cannot use this command on a **non-NSFW** channel.").queue()
        else {
            try {
                command.execute(context)
            } catch (ex: Throwable) {
                SentryUtils.sendCommandException(context, command, ex)
            }
        }
    }

    private fun runSubCommand(context: CommandContext, command: SubCommand) {
        if (command.meta.isNsfw && !context.channel.isNSFW)
            context.send(Emojis.Nsfw, "You cannot use this command on a **non-NSFW** channel.").queue()
        else {
            try {
                context.args = context.args.subList(1, context.args.size)
                command.execute(context)
                //command.execute(CommandContext(context.event, context.args.subList(1, context.args.size), context.config))
            } catch (ex: Exception) {
                SentryUtils.sendCommandException(context, command, ex)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CommandRegistry::class.java)
        private const val EMOJI_COOLDOWN = "⌚"
    }
}