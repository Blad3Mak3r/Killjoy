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

@file:Suppress("unused")

package dev.killjoy.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import dev.killjoy.Versions
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.abs.SubCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import org.slf4j.Logger
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Utils {

    fun printBanner(pid: Long, logger: Logger) {
        val content = """
            Starting Killjoy with PID $pid
             ___  __    ___  ___       ___             ___  ________      ___    ___ 
            |\  \|\  \ |\  \|\  \     |\  \           |\  \|\   __  \    |\  \  /  /|
            \ \  \/  /|\ \  \ \  \    \ \  \          \ \  \ \  \|\  \   \ \  \/  / /
             \ \   ___  \ \  \ \  \    \ \  \       __ \ \  \ \  \\\  \   \ \    / / 
              \ \  \\ \  \ \  \ \  \____\ \  \____ |\  \\_\  \ \  \\\  \   \/  /  /  
               \ \__\\ \__\ \__\ \_______\ \_______\ \________\ \_______\__/  / /    
                \|__| \|__|\|__|\|_______|\|_______|\|________|\|_______|\___/ /     
                                                The best Valorant bot!! \|___|/     
                 
            • Killjoy Version    :   ${Versions.KILLJOY}
            • Build Number       :   ${Versions.BUILD_NUMBER}
            • Commit             :   ${Versions.COMMIT}
            • JDA Version        :   ${Versions.JDA}
            =========================================================================
        """.trimIndent()

        logger.info(content)
    }

    fun newThreadFactory(name: String,
                         corePoolSize: Int,
                         maximumPoolSize: Int,
                         keepAliveTime: Long = 5L,
                         unit: TimeUnit = TimeUnit.MINUTES,
                         daemon: Boolean = true
    ): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            corePoolSize, maximumPoolSize,
            keepAliveTime, unit,
            LinkedBlockingQueue(),
            ThreadFactoryBuilder().setNameFormat(name).setDaemon(daemon).build())
    }

    fun newCoroutineDispatcher(name: String,
                         corePoolSize: Int,
                         maximumPoolSize: Int,
                         keepAliveTime: Long = 5L,
                         unit: TimeUnit = TimeUnit.MINUTES,
                         daemon: Boolean = true
    ): CoroutineDispatcher {
        return ThreadPoolExecutor(
            corePoolSize, maximumPoolSize,
            keepAliveTime, unit,
            LinkedBlockingQueue(),
            ThreadFactoryBuilder().setNameFormat(name).setDaemon(daemon).build()).asCoroutineDispatcher()
    }

    @JvmStatic
    fun isUrl(str: String): Boolean {
        return try {
            URL(str)
            true
        } catch (ignored: MalformedURLException) {
            false
        }
    }

    object Commands {

        @JvmStatic
        fun getSubCommand(invoke: String, subCommands: List<SubCommand>): SubCommand? {
            if (subCommands.isNullOrEmpty()) return null

            for (scmd in subCommands) {
                if (scmd.props.name == invoke.lowercase()) return scmd
            }

            return null
        }

        @JvmStatic
        fun replyWrongUsage(ctx: CommandContext, command: SubCommand) {
            ctx.send(Emojis.NoEntry, "This is not the correct way to use this command, use **joy help ${command.parent.props.name}** for more information.").queue()
        }

        @JvmStatic
        fun replyWrongUsage(ctx: CommandContext, command: Command) {
            ctx.send(Emojis.NoEntry, "This is not the correct way to use this command, use **joy help ${command.props.name}** for more information.").queue()
        }
    }

    object JDA {
        @JvmStatic
        fun getGuildRole(guild: Guild, role: String): Role? {
            return guild.getRolesByName(role, true).takeIf { it.isNotEmpty() }?.get(0)
        }

        @JvmStatic
        fun getGuildRoles(guild: Guild, roles: Collection<String>) = roles
            .filter { it.startsWith("<@&") }
            .map { it.replace("<@&", "") }
            .map { it.replace(">", "") }.mapNotNull { guild.getRoleById(it) }

        @JvmStatic
        fun getGuildRoles(guild: Guild, vararg roles: String) = getGuildRoles(guild, roles.toList())
    }

    object StringUtils {
        @JvmStatic
        fun capitalize(str: String) = str.capitalize()
    }

    object Validation {

        @JvmStatic
        fun startWith(str: String, vararg options: String): Boolean {
            for (opt in options) {
                if (str.startsWith(opt)) return true
            }
            return false
        }

        @JvmStatic
        fun startWithInt(str: String) = isInt(str.split("")[0])

        @JvmStatic
        fun isInt(str: String): Boolean = try {
            str.toInt()
            true
        } catch (ignored: NumberFormatException) {
            false
        }

        @JvmStatic
        fun isNull(str: String?) = str == null || str == "null"

        @JvmStatic
        fun validLength(str: String, max: Int) = str.length <= max
    }
}