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
import dev.killjoy.apis.riot.entities.RankedPlayerList
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.cache.RedisCache
import dev.killjoy.extensions.redisson.awaitSuspend
import io.sentry.Sentry
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class LeaderboardsModule(
    override val client: RedissonClient,
    override val expirationTTL: Long = 10L,
    override val expirationTTLUnit: TimeUnit = TimeUnit.MINUTES
) : IRedisModule {

    private fun getBucket(region: Region) = client.getBucket<RankedPlayerList>("killjoy:leaderboard:${region.name.lowercase()}")

    suspend fun get(region: Region): RankedPlayerList {
        val cached = getBucket(region)

        if (cached.isExistsAsync.awaitSuspend()) return cached.async.awaitSuspend()

        val result = RiotAPI.LeaderboardsAPI.top10(region)

        set(region, result)

        return result
    }

    private fun set(region: Region, list: RankedPlayerList) {
        getBucket(region).setAsync(list, expirationTTL, expirationTTLUnit)
            .thenAccept {
                logger.info("Successfully cached ${region.name.uppercase()} leaderboards. [TTL ${representTTL()}]")
            }
            .exceptionally {
                logger.error("Exception trying to cache ${region.name.uppercase()} leaderboards: ${it.message}")
                Sentry.captureException(it)
                null
            }
    }

    companion object {
        private val logger = RedisCache.logger
    }
}