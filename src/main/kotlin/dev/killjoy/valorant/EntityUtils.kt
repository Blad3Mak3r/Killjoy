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
import dev.killjoy.valorant.arsenal.ValorantWeapon
import org.json.JSONObject

typealias I18nMap = Map<String, String>

private val VALID_NAMES = I18n.VALID_LOCALES.map { it.language }

fun buildI18nMap(json: JSONObject, lowerCase: Boolean = false): I18nMap {
    val map = HashMap<String, String>()

    for (name in VALID_NAMES) {
        map[name] = json.getString(name).let { if (lowerCase) it.lowercase() else it }
    }

    return map
}

fun buildNullableI18nMap(json: JSONObject, key: String, lowerCase: Boolean = false): I18nMap? {
    if (!json.has(key)) return null

    val map = HashMap<String, String>()

    val jsonObject = json.getJSONObject(key)

    for (name in VALID_NAMES) {
        map[name] = jsonObject.getString(name).let { if (lowerCase) it.lowercase() else it }
    }

    return map
}

fun buildWeaponDescriptions(json: JSONObject): Map<String, List<String>> {
    val map = HashMap<String, List<String>>()

    for (name in VALID_NAMES) {
        map[name] = json.getJSONArray(name).map { "$it" }
    }
    return map
}

fun buildWeaponIDs(weapon: ValorantWeapon): List<String> {
    val list = mutableListOf<String>()
    list.add(weapon.name.lowercase().replace(" ", "").replace("-", ""))

    val locatedNames = weapon.locatedNames
    if (locatedNames != null) {
        list.addAll(locatedNames.map {
            it.value.lowercase().replace(" ", "").replace("-", "")
        })
    }

    return list
}

fun buildWeaponNames(weapon: ValorantWeapon): List<String> {
    val list = mutableListOf<String>()
    list.add(weapon.name)

    val locatedNames = weapon.locatedNames
    if (locatedNames != null) list.addAll(locatedNames.map { it.value })
    return list
}