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

package dev.killjoy.valorant.agent

import dev.killjoy.i18n.I18n
import dev.killjoy.i18n.I18nKey
import net.dv8tion.jda.api.entities.Guild

enum class AgentRole(val emoji: String, val iconUrl: String, private val i18nKey: I18nKey) {
    Controller("<:controller:754676227809214485>", "https://i.imgur.com/V4Ci1Oh.png", I18nKey.AGENT_CLASS_CONTROLLER),
    Duelist("<:duelist:754676227952083025>", "https://i.imgur.com/rs0d2qx.png", I18nKey.AGENT_CLASS_DUELIST),
    Initiator("<:initiator:754676227582722062>", "https://i.imgur.com/hCVcqgf.png", I18nKey.AGENT_CLASS_INITIATOR),
    Sentinel("<:sentinel:754676227994026044>", "https://i.imgur.com/ODX86kl.png", I18nKey.AGENT_CLASS_SENTINEL);

    fun locatedName(guild: Guild): String = I18n.getTranslate(guild, this.i18nKey)

    val snowFlake: String
        get() = emoji.removePrefix("<").removeSuffix(">")

    companion object {
        fun of(str: String): AgentRole {
            return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid role name.")
        }
    }
}