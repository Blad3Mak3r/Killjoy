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

import dev.killjoy.cache.modules.AgentStatsModule
import dev.killjoy.cache.modules.LeaderboardsModule
import dev.killjoy.cache.modules.MemesModule
import dev.killjoy.cache.modules.NewsModule
import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.config.SingleServerConfig
import org.slf4j.LoggerFactory

class RedisCache private constructor(config: Config) {
    private val client = Redisson.create(config)

    val agentStats = AgentStatsModule(client)
    val leaderboards = LeaderboardsModule(client)
    val memes = MemesModule(client)
    val news = NewsModule(client)

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

        internal val logger = LoggerFactory.getLogger(RedisCache::class.java)
    }
}