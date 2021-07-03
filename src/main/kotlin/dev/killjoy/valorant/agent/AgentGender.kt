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

import dev.killjoy.i18n.I18nKey

enum class AgentGender(val i18nKey: I18nKey) {
    Female(I18nKey.AGENT_GENDER_FEMALE),
    Male(I18nKey.AGENT_GENDER_MALE);

    companion object {
        fun of(string: String) = values().find { it.name.equals(string, true) } ?: error("Gender with key $string not found.")
    }
}