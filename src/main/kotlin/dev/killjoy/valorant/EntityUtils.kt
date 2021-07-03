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

package dev.killjoy.valorant

import dev.killjoy.i18n.I18n
import org.json.JSONObject

typealias I18nMap = Map<String, String>

fun buildI18nMap(json: JSONObject, lowerCase: Boolean = false): I18nMap {
    val names = I18n.VALID_LOCALES.map { it.language }
    val map = HashMap<String, String>()

    for (name in names) {
        map[name] = json.getString(name).let { if (lowerCase) it.lowercase() else it }
    }

    return map
}