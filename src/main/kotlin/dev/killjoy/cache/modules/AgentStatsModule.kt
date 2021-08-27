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

package dev.killjoy.cache.modules

import dev.killjoy.apis.riot.RiotAPI
import dev.killjoy.apis.riot.entities.AgentStats
import dev.killjoy.cache.RedisCache
import dev.killjoy.extensions.redisson.awaitSuspend
import io.sentry.Sentry
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

class AgentStatsModule(
    override val client: RedissonClient,
    override val expirationTTL: Long = 1L,
    override val expirationTTLUnit: TimeUnit = TimeUnit.HOURS
) : IRedisModule {

    private val bucket = client.getMap<String, AgentStats>("killjoy:agent-stats")

    suspend fun exists() = bucket.isExistsAsync.awaitSuspend()

    suspend fun get(name: String): AgentStats? {
        val cached = bucket.getAsync(name.replace("/", "").lowercase()).awaitSuspend()

        if (cached != null) return cached

        val results = RiotAPI.AgentStatsAPI.getAgentStatsAsync().await()

        set(results.associateBy {
            it.key.lowercase()
        })

        return results.find { it.key.equals(name, true) }
    }

    private fun set(stats: Map<String, AgentStats>) {
        bucket.putAllAsync(stats)
            .thenCompose {
                bucket.expireAsync(expirationTTL, expirationTTLUnit)
            }
            .thenAccept {
                if (it) {
                    logger.info("Successfully cached ${stats.size} agent stats. [TTL ${representTTL()}]")
                } else {
                    RedisCache.logger.info("Successfully cached ${stats.size} agent stats.")
                }
            }.exceptionally {
                RedisCache.logger.error("Exception trying to cache ${stats.size} agent stats: ${it.message}")
                Sentry.captureException(it)
                null
            }
    }

    companion object {
        private val logger = RedisCache.logger
    }
}