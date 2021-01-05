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
package tv.blademaker.killjoy.framework.abs

import net.dv8tion.jda.api.Permission
import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.annotations.CommandMeta
import java.util.concurrent.atomic.AtomicInteger

abstract class Command {
    @JvmField
    val meta: CommandMeta = this.javaClass.getAnnotation(CommandMeta::class.java)

    private val hits = AtomicInteger(0)

    @Throws(Exception::class)
    suspend fun execute(ctx: CommandContext) {
        hits.incrementAndGet()
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

    open val args: List<CommandArgument>
        get() = listOf()

    open val subCommands: List<SubCommand>
        get() = listOf()

    private fun userPermissions(): Set<Permission> {
        val permissions = hashSetOf(*meta.userPermissions)
        permissions.addAll(meta.category.permissions)
        return permissions
    }

    private fun botPermissions(): Set<Permission> {
        val permissions = hashSetOf(*meta.botPermissions)
        permissions.addAll(meta.category.botPermissions)
        return permissions
    }

    val userPermissions = userPermissions()
    val botPermissions = botPermissions()

    fun getHits() = hits.get()
}