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

import dev.killjoy.apis.news.NewsRetriever
import dev.killjoy.apis.news.ValorantNew
import dev.killjoy.cache.RedisCache
import dev.killjoy.extensions.redisson.awaitSuspend
import io.sentry.Sentry
import org.redisson.api.RedissonClient
import java.util.*
import java.util.concurrent.TimeUnit

class NewsModule(
    override val client: RedissonClient,
    override val expirationTTL: Long = 30,
    override val expirationTTLUnit: TimeUnit = TimeUnit.MINUTES
) : IRedisModule {

    private fun getLocatedList(locale: Locale) = client.getList<ValorantNew>("killjoy:news:${locale.language.lowercase()}")

    suspend fun exists(locale: Locale): Boolean = getLocatedList(locale).isExistsAsync.awaitSuspend()

    suspend fun get(locale: Locale): List<ValorantNew> {
        val cached = getLocatedList(locale)

        if (cached.isExistsAsync.awaitSuspend()) return cached.readAllAsync().awaitSuspend()

        val result = NewsRetriever.retrieveNews(locale).takeIf { it.isNotEmpty() }
            ?: error("Received empty news list for locale ${locale.language.uppercase()}")

        set(locale, result)

        return result
    }

    fun set(locale: Locale, list: List<ValorantNew>) {
        val rList = getLocatedList(locale)
        rList.addAllAsync(list)
            .thenCompose {
                rList.expireAsync(expirationTTL, expirationTTLUnit)
            }
            .thenAccept {
                if (it) {
                    logger.info("Successfully cached ${locale.language.uppercase()} news. [TTL ${representTTL()}]")
                } else {
                    logger.info("Successfully cached ${locale.language.uppercase()} news.")
                }
            }
            .exceptionally {
                logger.error("Exception trying to cache ${locale.language.uppercase()} news: ${it.message}")
                Sentry.captureException(it)
                null
            }
    }

    companion object {
        private val logger = RedisCache.logger
    }

}