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

package dev.killjoy.cache.internal

import dev.killjoy.database.models.AccountWithStats
import dev.killjoy.extensions.redisson.awaitSuspend
import dev.killjoy.services.PlayerCard
import dev.killjoy.valorant.agent.ValorantAgent
import io.sentry.Sentry
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.User
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class PlayerCardCache(
    override val client: RedissonClient,
    override val expirationTTL: Long = 30L,
    override val expirationTTLUnit: TimeUnit = TimeUnit.MINUTES
) : IRedisCache {

    private fun getBucket(user: User) = client.getBinaryStream("killjoy:player-card:${user.id}")

    suspend fun get(user: User, aws: AccountWithStats, agent: ValorantAgent): ByteArray {
        val bucket = getBucket(user)

        if (bucket.isExistsAsync.awaitSuspend()) return bucket.async.awaitSuspend()

        val result = PlayerCard.generate(aws, agent).await()

        set(result, user)

        return result
    }

    private fun set(byteArray: ByteArray, user: User) {
        getBucket(user).setAsync(byteArray, expirationTTL, expirationTTLUnit)
            .thenAccept {
                logger.info("Successfully cached ${user.id} player card.")
            }
            .exceptionally {
                logger.error("Exception caching ${user.id} player card: ${it.message}")
                Sentry.captureException(it)
                null
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PlayerCardCache::class.java)
    }
}