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

package tv.blademaker.killjoy.apis.riot.entities

data class RankedPlayer(
    val puuid: String,
    val gameName: String,
    val tagLine: String,
    val leaderboardRank: Int,
    val rankedRating: Int,
    val numberOfWins: Int
) {
    val fullNameTag = "$gameName#$tagLine"
}

data class RankedPlayerList(
    val updatedAt: Long,
    val players: List<RankedPlayer>
)
