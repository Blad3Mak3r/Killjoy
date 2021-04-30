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

package dev.killjoy.bot.valorant

@Suppress("unused")
enum class Ranks(
        val id: Int,
        val emoji: String
) {
    Iron(       0, ":iron_3:771924280211537960"),
    Bronze(     1, ":bronze_3:771924279985176587"),
    Silver(     2, ":silver_3:771924279968268350"),
    Gold(       3, ":gold_3:771924280166187028>"),
    Platinum(   4, ":platinum_3:771924280245616650"),
    Diamond(    5, ":diamond_3:771924280131715092"),
    Inmortal(   6, ":inmortal_3:771924280144429057"),
    Radiant(    7, ":radiant:771924280425971762");

    val diple: String
        get() = "<${this.emoji}>"
}