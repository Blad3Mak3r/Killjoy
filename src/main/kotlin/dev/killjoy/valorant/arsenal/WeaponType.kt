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

package dev.killjoy.valorant.arsenal

import dev.killjoy.i18n.I18nKey

enum class WeaponType(
    val i18nKey: I18nKey
) {
    Smgs(I18nKey.ARSENAL_TYPE_SMGS),
    Rifles(I18nKey.ARSENAL_TYPE_RIFLES),
    Shotguns(I18nKey.ARSENAL_TYPE_SHOTGUNS),
    Snipers(I18nKey.ARSENAL_TYPE_SNIPERS),
    Melee(I18nKey.ARSENAL_TYPE_MELEE),
    Heavies(I18nKey.ARSENAL_TYPE_HEAVIES),
    Sidearms(I18nKey.ARSENAL_TYPE_SIDEARMS);

    companion object {
        fun of(str: String): WeaponType {
            return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid type name.")
        }
    }
}