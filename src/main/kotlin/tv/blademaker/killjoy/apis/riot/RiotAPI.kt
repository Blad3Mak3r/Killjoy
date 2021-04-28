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

package tv.blademaker.killjoy.apis.riot

import com.github.benmanes.caffeine.cache.Caffeine
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.apis.riot.entities.AgentStats
import tv.blademaker.killjoy.apis.riot.entities.RankedPlayer
import tv.blademaker.killjoy.apis.riot.entities.RankedPlayerList
import tv.blademaker.killjoy.apis.riot.entities.Region
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

object RiotAPI {

    const val CURRENT_ACT_ID = "52e9749a-429b-7060-99fe-4595426a0cf7"   // Episode 2 (Act 3)
    private val LOGGER = LoggerFactory.getLogger(RiotAPI::class.java)

    object LeaderboardsAPI {
        private val leaderboardsCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build<String, RankedPlayerList>()

        suspend fun getCurrentTop20(region: Region): RankedPlayerList {

            val cached = leaderboardsCache.getIfPresent(region.name.toUpperCase())
            if (cached != null) return cached

            LOGGER.info("Retrieving fresh leaderboards for ${region.name.toUpperCase()}")

            val url = "https://dgxfkpkb4zk5c.cloudfront.net/leaderboards/affinity/${region.name.toUpperCase()}/queue/competitive/act/$CURRENT_ACT_ID?startIndex=0&size=10"
            val r = Unirest.get(url).asJsonAsync().await()

            check(r.isSuccess) { "Not success status: ${r.status}" }

            val content = r.body.`object`

            val list = content.getJSONArray("players").map {
                it as JSONObject
                RankedPlayer(
                    it.getString("puuid"),
                    it.getString("gameName"),
                    it.getString("tagLine"),
                    it.getInt("leaderboardRank"),
                    it.getInt("rankedRating"),
                    it.getInt("numberOfWins")
                )
            }.sortedBy { p -> p.leaderboardRank }

            val rankedList = RankedPlayerList(System.currentTimeMillis(), list)
            leaderboardsCache.put(region.name.toUpperCase(), rankedList)
            return rankedList
        }
    }

    object AgentStatsAPI {
        private val SCRIPT_REGEX = "(window\\.__statsByAgent = ([\\S\\s]+);)".toPattern()

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

            return agentStatsCache[agent.toLowerCase()]
        }

        suspend fun getAgentStatsAsync(): Deferred<List<AgentStats>> = withContext(
            Dispatchers.IO) {
            async { getAgentStats() }
        }

        suspend fun getAgentStatsAsync(agent: String): Deferred<AgentStats?> = withContext(
            Dispatchers.IO)  {
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

            val array = convertStatsObjectToList(org.json.JSONObject(matcher.group(2)))

            val agentStats = array.map { AgentStats(it) }

            for (stat in agentStats) {
                agentStatsCache[stat.key.toLowerCase()] = stat
            }

            lastUpdate = (System.currentTimeMillis() + Duration.ofMinutes(10).toMillis())

            LOGGER.info("A total of ${agentStats.size} agents stats has been updated.")
        }

        private fun convertStatsObjectToList(jsonObject: org.json.JSONObject): List<org.json.JSONObject> {
            val jsonArray = mutableListOf<org.json.JSONObject>()
            for (name in jsonObject.names().map { it as String }) {
                jsonArray.add(jsonObject.getJSONObject(name))
            }
            return jsonArray
        }
    }
}