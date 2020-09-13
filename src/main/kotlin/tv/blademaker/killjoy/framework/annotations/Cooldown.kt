/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.framework.annotations

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cooldown(
        val value: Long = 3,
        val timeUnit: TimeUnit = TimeUnit.SECONDS,
        val type: Type = Type.User
) {
    enum class Type {
        User, Guild;
    }
}