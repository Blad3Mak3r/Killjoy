package tv.blademaker.killjoy

import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.sharding.ShardManager
import net.hugebot.ratelimiter.RateLimiter
import org.json.JSONArray
import org.json.JSONObject
import tv.blademaker.killjoy.core.Agent
import java.util.concurrent.TimeUnit

object Launcher {

    lateinit var shardManager: ShardManager
        private set

    lateinit var config: Any
        private set

    lateinit var info: ApplicationInfo
        private set

    lateinit var agentsCollection: List<Agent>
        private set

    val rateLimiter: RateLimiter = RateLimiter.Builder().setQuota(20).setExpirationTime(1, TimeUnit.MINUTES).build()

    @JvmStatic
    fun main(args: Array<String>) {

        agentsCollection = loadAgents()


    }

    private fun loadAgents(): List<Agent> {
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

        return list
    }
}