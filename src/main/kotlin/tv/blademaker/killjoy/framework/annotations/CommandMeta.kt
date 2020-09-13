/*
 * Copyright (c) 2020.
 * BladeMaker
 */
package tv.blademaker.killjoy.framework.annotations

import net.dv8tion.jda.api.Permission
import tv.blademaker.killjoy.framework.Category

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandMeta(
        val name: String,
        val category: Category,
        val isNsfw: Boolean = false,
        val aliases: Array<String> = [],
        val cooldown: Cooldown = Cooldown(3),
        val userPermissions: Array<Permission> = [],
        val botPermissions: Array<Permission> = []
)