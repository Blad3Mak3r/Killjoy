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
import org.json.JSONObject

enum class WallPenetration(val i18nKey: I18nKey) {
    Low(I18nKey.ARSENAL_WALLPENETRATION_LOW),
    Medium(I18nKey.ARSENAL_WALLPENETRATION_MEDIUM),
    High(I18nKey.ARSENAL_WALLPENETRATION_HIGH);

    companion object {
        fun of(json: JSONObject): WallPenetration? {
            if (!json.has("wall_penetration")) return null

            return values().find { it.name.equals(json.getString("wall_penetration"), true) }
        }
    }
}