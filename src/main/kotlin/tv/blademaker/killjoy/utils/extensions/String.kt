/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

import tv.blademaker.killjoy.utils.Utils

fun String.isUrl() = Utils.isUrl(this)
fun String.isInt() = Utils.Validation.isInt(this)

fun String.isMemberMention() = this matches MEMBER_REGEX
fun String.isChannelMention() = this matches CHANNEL_REGEX
fun String.isRoleMention() = this matches ROLE_REGEX

@Throws(IllegalArgumentException::class)
fun String.asSnowflake(): String {
    return when {
        this.isMemberMention() -> MEMBER_REGEX.find(this)?.groupValues?.get(1) ?: throw IllegalArgumentException("\"$this\" is not a valid member mention")
        this.isChannelMention() -> CHANNEL_REGEX.find(this)?.groupValues?.get(1) ?: throw IllegalArgumentException("\"$this\" is not a valid channel mention")
        this.isRoleMention() -> ROLE_REGEX.find(this)?.groupValues?.get(1) ?: throw IllegalArgumentException("\"$this\" is not a valid role mention")
        else -> throw IllegalArgumentException("This ID type is not supported")
    }
}