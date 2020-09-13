/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

val MEMBER_REGEX = "^<@!?(?<id>[0-9]{17,19})>".toRegex()
val CHANNEL_REGEX = "^<#(?<id>[0-9]{17,19})>".toRegex()
val ROLE_REGEX = "^<@&(?<id>[0-9]{17,19})>".toRegex()