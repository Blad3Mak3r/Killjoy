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

package tv.blademaker.killjoy.utils

import org.json.JSONObject
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.valorant.Agent
import tv.blademaker.killjoy.valorant.Weapon

object Loaders {

    private val log = LoggerFactory.getLogger(Loaders::class.java)

    fun loadAgents(): List<Agent> {
        val list = mutableListOf<Agent>()
        val agentsIndex = this::class.java.getResource("/agents/agents.txt").readText().split("\n")
        if (agentsIndex.isEmpty()) throw IllegalStateException("Agents Index cannot be empty or null")

        for (agentName in agentsIndex) {
            val file = this::class.java.getResource("/agents/${agentName.toLowerCase()}.json")
                ?: throw IllegalStateException("$agentName.json is not present")
            val fileContent = file.readText()
            if (fileContent.isEmpty()) throw IllegalStateException("$agentName.json is empty")

            val agent = Agent(JSONObject(fileContent))

            if (list.any { it.id == agent.id || it.name.equals(agent.name, true) || it.number == agent.number })
                throw IllegalStateException("Agent with id ${agent.id} or name ${agent.name} is already present.")

            list.add(agent)
        }

        log.info("Loaded ${list.size} agents!! [${list.joinToString(", ") { it.name }}]")
        Agent.StatsMapper.doUpdate()
        return list
    }

    fun loadArsenal(): List<Weapon> {
        val list = mutableListOf<Weapon>()
        val arsenalIndex = this::class.java.getResource("/arsenal/arsenal.txt").readText().split("\n")
        if (arsenalIndex.isEmpty()) throw IllegalStateException("Arsenal Index cannot be empty or null")

        for (weaponName in arsenalIndex) {
            val file = this::class.java.getResource("/arsenal/${weaponName.toLowerCase()}.json")
                    ?: throw IllegalStateException("$weaponName.json is not present")
            val fileContent = file.readText()
            if (fileContent.isEmpty()) throw IllegalStateException("$weaponName.json is empty")
            list.add(Weapon(JSONObject(fileContent)))
        }

        log.info("Loaded ${list.size} weapons from arsenal!! [${list.joinToString(", ") { it.name }}]")
        return list
    }
}