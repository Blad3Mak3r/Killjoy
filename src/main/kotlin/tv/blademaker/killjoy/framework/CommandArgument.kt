/*
 * Copyright (c) 2020.
 * BladeMaker
 */
package tv.blademaker.killjoy.framework

data class CommandArgument(
        val name: String,
        val info: String,
        val isRequired: Boolean = false
)