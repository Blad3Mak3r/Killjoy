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

package dev.killjoy.apis.riot

import dev.killjoy.apis.riot.entities.AgentStats
import dev.killjoy.apis.riot.entities.RankedPlayer
import dev.killjoy.apis.riot.entities.RankedPlayerList
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.utils.HttpUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

object RiotAPI {

    const val CURRENT_ACT_ID = "a16955a5-4ad0-f761-5e9e-389df1c892fb"   // Episode 3 (Act 3)
    private val LOGGER = LoggerFactory.getLogger(RiotAPI::class.java)

    object LeaderboardsAPI {
        private fun buildURL(region: Region): String {
            return "https://dgxfkpkb4zk5c.cloudfront.net/leaderboards/affinity/${region.name.lowercase()}/queue/competitive/act/$CURRENT_ACT_ID?startIndex=0&size=10"
        }

        suspend fun top10(region: Region): RankedPlayerList {
            LOGGER.info("Retrieving fresh leaderboards for ${region.name.uppercase()}")

            val req = HttpUtils.await(JSONObject::class.java) {
                url(buildURL(region))
            }
            val content = req.content

            check(req.isSuccessful) { "Not success status: ${req.code}" }
            check(content != null) { "Received empty body." }

            val list = content.getJSONArray("players")
                .mapNotNull { RankedPlayer.opt(it as JSONObject) }
                .sortedBy { p -> p.leaderboardRank }

            return RankedPlayerList(System.currentTimeMillis(), list)
        }
    }

    object AgentStatsAPI {
        private val SCRIPT_REGEX = "(window\\.__statsByAgent = ([\\S\\s]+);)".toPattern()

        val cached: Boolean
            get() = agentStatsCache.isNotEmpty() && !outdated

        @Volatile
        private var lastUpdate = 0L

        private val agentStatsCache = HashMap<String, AgentStats>()

        private val outdated: Boolean
            get() = lastUpdate < System.currentTimeMillis()

        private fun getAgentStats() = fetchAgentStats()

        private fun getAgentStats(agent: String) = fetchAgentStats().find {
            it.key.equals(agent.lowercase().replace("/", ""), true)
        }

        suspend fun getAgentStatsAsync(): Deferred<List<AgentStats>> = withContext(Dispatchers.IO) {
            async { getAgentStats() }
        }

        suspend fun getAgentStatsAsync(agent: String): Deferred<AgentStats?> = withContext(Dispatchers.IO)  {
            async { getAgentStats(agent) }
        }

        private fun fetchAgentStats(): List<AgentStats> {
            val doc = Jsoup.connect("https://dak.gg/valorant/statistics/agents")
                .timeout(10000)
                .get()

            val script = doc.getElementsByTag("script")
                .filter { s -> s.data().isNotBlank() }
                .find { s -> s.data().contains("window.__statsByAgent") }

            checkNotNull(script) { "Agent stats script not found." }

            val matcher = SCRIPT_REGEX.matcher(script.data())

            check(matcher.find(2)) { "Script does not matches regular expresion $SCRIPT_REGEX" }

            val array = convertStatsObjectToList(JSONObject(matcher.group(2)))

            return array.map { AgentStats(it) }
        }

        private fun convertStatsObjectToList(jsonObject: JSONObject): List<JSONObject> {
            val jsonArray = mutableListOf<JSONObject>()
            for (name in jsonObject.names().map { it as String }) {
                jsonArray.add(jsonObject.getJSONObject(name))
            }
            return jsonArray
        }
    }
}