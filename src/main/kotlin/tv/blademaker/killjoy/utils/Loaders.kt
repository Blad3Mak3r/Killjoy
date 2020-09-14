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

            list.add(Agent(JSONObject(fileContent)))
        }

        log.info("Loaded ${list.size} agents!! [${list.joinToString(", ") { it.name }}]")

        return list
    }

    private fun loadMaps() {}

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