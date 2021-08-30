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

import dev.killjoy.apis.memes.Meme
import dev.killjoy.apis.memes.RedditMemes
import dev.killjoy.cache.RedisCache
import dev.killjoy.extensions.redisson.awaitSuspend
import io.sentry.Sentry
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

class MemesModule(
    override val client: RedissonClient,
    override val expirationTTL: Long = 10L,
    override val expirationTTLUnit: TimeUnit = TimeUnit.MINUTES
) : IRedisModule {

    private fun getBucket(subreddit: String) = client.getList<Meme>("killjoy:memes:${subreddit.lowercase()}")

    suspend fun get(subreddit: String): Meme {
        val bucket = getBucket(subreddit)
        val cached = bucket.isExistsAsync.awaitSuspend()

        val memes = if (!cached) {
            val newMemes = RedditMemes.retrieveValidMemes(subreddit)
            set(subreddit, newMemes)
            newMemes
        } else {
            bucket.readAllAsync().awaitSuspend()
        }

        return RedditMemes.select(memes)
    }

    private fun set(subreddit: String, memes: List<Meme>) {
        val bucket = getBucket(subreddit)
        bucket.addAllAsync(memes)
            .thenCompose {
                bucket.expireAsync(expirationTTL, expirationTTLUnit)
            }
            .thenAccept {
                RedisCache.logger.info("Successfully cached a total of ${memes.size} memes from $subreddit")
            }
            .exceptionally {
                RedisCache.logger.error("Cannot cache memes for $subreddit: ${it.message}", it)
                Sentry.captureException(it)
                null
            }
    }



}