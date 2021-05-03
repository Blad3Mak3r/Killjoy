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
package dev.killjoy.bot.framework.abs

import dev.killjoy.bot.framework.CommandContext
import dev.killjoy.bot.framework.annotations.CommandArgument
import dev.killjoy.bot.framework.annotations.CommandProperties
import net.dv8tion.jda.api.Permission

abstract class Command {
    @JvmField
    val props: CommandProperties = this.javaClass.getAnnotation(CommandProperties::class.java)

    @Throws(Exception::class)
    suspend fun execute(ctx: CommandContext) {
        /*if (this.javaClass.isAnnotationPresent(PremiumCommand::class.java)) {
            //val lvlRequired = this.javaClass.getAnnotation(PremiumCommand::class.java).value

            // TODO: Return premium embed
            /*if (ctx.guildConfig.premium == null || ctx.guildConfig.premium == 0) {
                return
            }*/
            // if (ctx.guildConfig.premium >= lvlRequired) handle(ctx)
            handle(ctx)
        } else if (checks(ctx)) handle(ctx)*/

        if (checks(ctx)) handle(ctx)
    }

    protected open suspend fun checks(ctx: CommandContext): Boolean {
        return true
    }

    @Throws(Exception::class)
    protected abstract suspend fun handle(ctx: CommandContext)

    open val help: String
        get() = "Not help provided for this command."

    open val args: Array<CommandArgument>
        get() = props.arguments

    private val subCommands: MutableList<SubCommand> = mutableListOf()
    fun getSubCommands(): List<SubCommand> = subCommands
    internal fun registerSubCommand(subCommand: SubCommand) {
        check(!(subCommands.any { it.props.name == subCommand.props.name })) {
            "Sub-command with name ${subCommand.props.name} is already registered in command ${props.name}"
        }

        subCommands.add(subCommand)
    }

    private fun userPermissions(): Set<Permission> {
        val permissions = hashSetOf(*props.userPermissions)
        permissions.addAll(props.category.permissions)
        return permissions
    }

    private fun botPermissions(): Set<Permission> {
        val permissions = hashSetOf(*props.botPermissions)
        permissions.addAll(props.category.botPermissions)
        return permissions
    }

    val userPermissions = userPermissions()
    val botPermissions = botPermissions()
}