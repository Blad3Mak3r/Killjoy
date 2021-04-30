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
import dev.killjoy.bot.framework.annotations.SubCommandProperties
import java.util.concurrent.atomic.AtomicInteger

abstract class SubCommand(@JvmField val parent: Command) {

    @JvmField
    val props: SubCommandProperties = this::class.java.getAnnotation(SubCommandProperties::class.java)
    val hits = AtomicInteger(0)

    open val help: String
        get() = "No help provided for this sub-command"

    open val args: Array<CommandArgument>
        get() = props.arguments

    @Throws(Exception::class)
    suspend fun execute(ctx: CommandContext) {
        hits.incrementAndGet()
        if (checks(ctx)) handle(ctx)
    }

    internal open suspend fun checks(ctx: CommandContext): Boolean = true

    @Throws(Exception::class)
    protected abstract suspend fun handle(ctx: CommandContext)

}