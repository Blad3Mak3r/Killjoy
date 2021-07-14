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

package tv.blademaker.slash.utils

import dev.killjoy.extensions.toHuman
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.AbstractSlashCommand
import tv.blademaker.slash.api.SlashCommandContext
import tv.blademaker.slash.api.annotations.Permissions
import java.lang.reflect.Modifier

object SlashUtils {

    private val LOGGER = LoggerFactory.getLogger(SlashUtils::class.java)

    enum class PermissionTarget {
        BOT, USER;
    }

    internal fun hasPermissions(ctx: SlashCommandContext, permissions: Permissions?): Boolean {
        if (permissions == null || permissions.bot.isEmpty() && permissions.user.isEmpty()) return true

        var member: Member = ctx.member

        // Check for the user permissions
        var guildPerms = member.hasPermission(permissions.user.toList())
        var channelPerms = member.hasPermission(ctx.channel, permissions.user.toList())

        if (!(guildPerms && channelPerms)) {
            replyRequiredPermissions(ctx, PermissionTarget.USER, permissions.user)
            return false
        }

        // Check for the bot permissions
        member = ctx.selfMember
        guildPerms = member.hasPermission(permissions.bot.toList())
        channelPerms = member.hasPermission(ctx.channel, permissions.bot.toList())

        if (!(guildPerms && channelPerms)) {
            replyRequiredPermissions(ctx, PermissionTarget.BOT, permissions.bot)
            return false
        }

        return true
    }

    private fun replyRequiredPermissions(
        ctx: SlashCommandContext,
        target: PermissionTarget,
        permissions: Array<Permission>
    ) {
        when(target) {
            PermissionTarget.BOT -> {
                val perms = permissions.toHuman()
                ctx.reply("\uD83D\uDEAB The bot does not have the necessary permissions to carry out this action." +
                        "\nRequired permissions: **${perms}**.")
            }
            PermissionTarget.USER -> {
                val perms = permissions.toHuman()
                ctx.reply("\uD83D\uDEAB You do not have the necessary permissions to carry out this action." +
                        "\nRequired permissions: **${perms}**.")
            }
        }.setEphemeral(true).queue()
    }

    fun discoverSlashCommands(packageName: String): List<AbstractSlashCommand> {
        val classes = Reflections(packageName, SubTypesScanner())
            .getSubTypesOf(AbstractSlashCommand::class.java)
            .filter { !Modifier.isAbstract(it.modifiers) && AbstractSlashCommand::class.java.isAssignableFrom(it) }

        LOGGER.info("Discovered a total of ${classes.size} slash commands in package $packageName")

        val commands = mutableListOf<AbstractSlashCommand>()

        for (clazz in classes) {
            val instance = clazz.getDeclaredConstructor().newInstance()
            val commandName = instance.commandName.lowercase()

            if (commands.any { it.commandName.equals(commandName, true) }) {
                throw IllegalStateException("Command with name $commandName is already registered.")
            }

            commands.add(instance)
        }

        return commands
    }

    fun parseOptionToString(option: OptionMapping): String {
        return "${option.name} (${optionToString(option)})"
    }

    private fun optionToString(option: OptionMapping): String {
        return try {
            when (option.type) {
                in LONG_TYPES -> option.asLong.toString()
                OptionType.BOOLEAN -> option.asBoolean.toString()
                else -> option.asString
            }
        } catch (e: Exception) {
            "EXCEPTION"
        }
    }

    private val LONG_TYPES = setOf(OptionType.CHANNEL, OptionType.ROLE, OptionType.USER, OptionType.INTEGER)

    @Suppress("unused")
    fun RestAction<*>.asEphemeral(): RestAction<*> {
        when(this) {
            is ReplyAction -> this.setEphemeral(true)
            is WebhookMessageAction<*> -> this.setEphemeral(true)
        }

        return this
    }
}