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

package dev.killjoy

import dev.killjoy.apis.memes.Meme
import dev.killjoy.apis.news.ValorantNew
import dev.killjoy.apis.riot.entities.RankedPlayerList
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.database.DatabaseConnection
import dev.killjoy.valorant.agent.AgentAbility
import dev.killjoy.valorant.agent.ValorantAgent
import dev.killjoy.valorant.arsenal.ValorantWeapon
import dev.killjoy.valorant.map.ValorantMap
import java.util.*

interface Killjoy {

    /**
     * Retrieve the active Database connection for Killjoy.
     */
    fun getDatabaseConnection(): DatabaseConnection

    /**
     * Retrieve all the agent abilities.
     *
     * @return List of [AgentAbility]
     */
    fun getAbilities(): List<AgentAbility>

    /**
     * Retrieve agent abilities.
     *
     * @param agentName The agent name formatted as [String].
     *
     * @return List of [AgentAbility]
     */
    fun getAbilities(agentName: String): List<AgentAbility>

    /**
     * Get an agent ability by name.
     *
     * @param name Ability name formatted as [String].
     *
     * @return [AgentAbility] **(nullable)**
     */
    fun getAbility(name: String): AgentAbility?

    /**
     * Get an agent by name or number.
     *
     * @param input Agent name or number formatted as [String].
     *
     * @return [ValorantAgent] **(nullable)**
     */
    fun getAgent(input: String): ValorantAgent?

    /**
     * Get a map by name.
     *
     * @param name map name formatted as [String].
     *
     * @return [ValorantMap] **(nullable)**
     */
    fun getMap(name: String): ValorantMap?

    /**
     * Get a weapon by name.
     *
     * @param name Weapon name formatted as [String].
     *
     * @return [ValorantWeapon] **(nullable)**
     */
    fun getWeapon(name: String): ValorantWeapon?

    /**
     * Get a weapon by ID.
     *
     * @param id Weapon ID formatted as [String].
     *
     * @return [ValorantWeapon] **(nullable)**
     */
    fun getWeaponById(id: String): ValorantWeapon?

    suspend fun getLeaderboard(region: Region): RankedPlayerList

    suspend fun getNews(locale: Locale): List<ValorantNew>

    suspend fun getMeme(subreddit: String): Meme

    fun shutdown(code: Int = 0)
}