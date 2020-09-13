/*
 * Copyright (c) 2020.
 * BladeMaker
 */
package tv.blademaker.killjoy.framework.annotations

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommandMeta(
        val name: String,
        val isNsfw: Boolean = false
)