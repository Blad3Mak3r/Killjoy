/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.framework.abs

import tv.blademaker.killjoy.framework.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.annotations.SubCommandMeta
import java.util.concurrent.atomic.AtomicInteger

abstract class SubCommand(@JvmField val parent: Command) {

    @JvmField
    val meta: SubCommandMeta = this::class.java.getAnnotation(SubCommandMeta::class.java)
    val hits = AtomicInteger(0)

    open val help: String
        get() = "No help provided for this sub-command"

    open val args: List<CommandArgument>
        get() = listOf()

    @Throws(Exception::class)
    fun execute(ctx: CommandContext) {
        hits.incrementAndGet()
        if (checks(ctx)) handle(ctx)
    }

    internal open fun checks(ctx: CommandContext): Boolean = true

    @Throws(Exception::class)
    protected abstract fun handle(ctx: CommandContext)

}