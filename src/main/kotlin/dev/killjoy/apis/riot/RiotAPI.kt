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

import com.github.benmanes.caffeine.cache.Caffeine
import dev.killjoy.Credentials
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
import java.time.Duration
import java.util.concurrent.TimeUnit

object RiotAPI {

    const val CURRENT_ACT_ID = "2a27e5d2-4d30-c9e2-b15a-93b8909a442c"   // Episode 2 (Act 1) TODO(Waiting for valorant fix)
    private val LOGGER = LoggerFactory.getLogger(RiotAPI::class.java)

    object LeaderboardsAPI {
        private val leaderboardsCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build<String, RankedPlayerList>()

        suspend fun getCurrentTop20(region: Region): RankedPlayerList {

            val cached = leaderboardsCache.getIfPresent(region.name.uppercase())
            if (cached != null) return cached

            LOGGER.info("Retrieving fresh leaderboards for ${region.name.uppercase()}")

            val req = HttpUtils.await(JSONObject::class.java) {
                url("https://${region.name.lowercase()}.api.riotgames.com/val/ranked/v1/leaderboards/by-act/$CURRENT_ACT_ID?startIndex=0&size=10")
                addHeader("X-Riot-Token", Credentials["riot.api_key"])
            }
            val content = req.content

            check(req.isSuccessful) { "Not success status: ${req.code}" }
            check(content != null) { "Received empty body." }

            val list = content.getJSONArray("players")
                .mapNotNull { RankedPlayer.opt(it as JSONObject) }
                .sortedBy { p -> p.leaderboardRank }

            val rankedList = RankedPlayerList(System.currentTimeMillis(), list)
            leaderboardsCache.put(region.name.uppercase(), rankedList)
            return rankedList
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

        private fun getAgentStats(): List<AgentStats> {
            if (agentStatsCache.isEmpty() || outdated) {
                updateAgentStats()
            }

            return agentStatsCache.values.toList()
        }

        private fun getAgentStats(agent: String): AgentStats? {
            if (agentStatsCache.isEmpty() || outdated) {
                updateAgentStats()
            }

            return agentStatsCache[agent.lowercase().replace("/", "")]
        }

        suspend fun getAgentStatsAsync(): Deferred<List<AgentStats>> = withContext(Dispatchers.IO) {
            async { getAgentStats() }
        }

        suspend fun getAgentStatsAsync(agent: String): Deferred<AgentStats?> = withContext(Dispatchers.IO)  {
            async { getAgentStats(agent) }
        }

        @Synchronized
        private fun updateAgentStats() {
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

            val agentStats = array.map { AgentStats(it) }

            for (stat in agentStats) {
                agentStatsCache[stat.key.lowercase()] = stat
            }

            lastUpdate = (System.currentTimeMillis() + Duration.ofMinutes(10).toMillis())

            LOGGER.info("A total of ${agentStats.size} agents stats has been updated.")
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