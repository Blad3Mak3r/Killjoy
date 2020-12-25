/*******************************************************************************
 * Copyright (c) 2020. Blademaker
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