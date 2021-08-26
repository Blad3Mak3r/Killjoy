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

package dev.killjoy.cache

import dev.killjoy.Credentials
import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.apis.riot.entities.AgentStats
import dev.killjoy.apis.riot.entities.RankedPlayerList
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.cache.internal.LeaderboardsCache
import dev.killjoy.cache.internal.NewsCache
import dev.killjoy.extensions.redisson.awaitSuspend
import io.sentry.Sentry
import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.config.SingleServerConfig
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class RedisCache private constructor(config: Config) {
    private val client = Redisson.create(config)

    val leaderboards = LeaderboardsCache(client)
    val news = NewsCache(client)

    private val agentStatsMap = client.getMap<String, AgentStats>("killjoy:agent-stats")

    suspend fun agentStatsExists(): Boolean = agentStatsMap.isExistsAsync.awaitSuspend()

    suspend fun getAgentStats(name: String): AgentStats? {
        val cached = agentStatsMap.getAsync(name.replace("/", "").lowercase()).awaitSuspend()

        if (cached != null) return cached

        val results = RiotAPI.AgentStatsAPI.getAgentStatsAsync().await()

        setAgentStats(results.associateBy {
            it.key.lowercase()
        })

        return results.find { it.key.equals(name, true) }
    }

    private fun setAgentStats(stats: Map<String, AgentStats>) {
        agentStatsMap.putAllAsync(stats)
            .thenCompose {
                agentStatsMap.expireAsync(AGENT_STATS_TTL, AGENT_STATS_TTL_UNIT)
            }
            .thenAccept {
                if (it) {
                    logger.info("Successfully cached ${stats.size} agent stats. [TTL ${AGENT_STATS_TTL_UNIT.toMillis(AGENT_STATS_TTL)}]")
                } else {
                    logger.info("Successfully cached ${stats.size} agent stats.")
                }
            }.exceptionally {
                logger.error("Exception trying to cache ${stats.size} agent stats: ${it.message}")
                Sentry.captureException(it)
                null
            }
    }

    fun shutdown() = client.shutdown()

    companion object {
        fun createSingleServer(initConfig: Config.() -> Unit, sscBuilder: SingleServerConfig.() -> Unit): RedisCache {
            val config = Config().apply(initConfig)
            config.useSingleServer().apply(sscBuilder)

            return RedisCache(config)
        }

        fun buildUrl(): String {
            val host = Credentials.getOrDefault("redis.host", "localhost")
            val port = Credentials.getOrDefault("redis.port", 6379)

            return "redis://$host:$port"
        }

        private const val AGENT_STATS_TTL = 1L
        private val AGENT_STATS_TTL_UNIT = TimeUnit.HOURS

        private val logger = LoggerFactory.getLogger(RedisCache::class.java)
    }
}