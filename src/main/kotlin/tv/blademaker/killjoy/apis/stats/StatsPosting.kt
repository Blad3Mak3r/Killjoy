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

package tv.blademaker.killjoy.apis.stats

import net.dv8tion.jda.api.sharding.ShardManager
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Suppress("unused")
class StatsPosting private constructor(
    private val shardManager: ShardManager,
    private val httpClient: OkHttpClient,
    private val threadPoolExecutor: ScheduledExecutorService,
    private val websites: List<Website>,
    private val initialDelay: Long,
    private val period: Long,
    private val timeUnit: TimeUnit
) {

    private var lastGuildCount = AtomicInteger(0)

    internal var task: ScheduledFuture<*>? = null

    private val totalShards = shardManager.shardsTotal

    init {
        logger.info("Enabled guild count for ${websites.joinToString(", ") { it.id }}!")

        shardManager.addEventListener(ReadyListener(shardManager, this))
    }

    internal fun createTask(): ScheduledFuture<*> {
        logger.info("Scheduling stats posting!")
        return threadPoolExecutor.scheduleAtFixedRate(schedule(), initialDelay, period, timeUnit)
    }

    private fun schedule() = Runnable {
        try {
            val botId = getBotId(shardManager)
            val guildCount = getShardGuildSize(shardManager)

            if (lastGuildCount.get() == guildCount) return@Runnable

            for (website in websites) {
                try {
                    website.postStats(httpClient, botId, guildCount)
                } catch (e: Exception) {
                    logger.error("Cannot post stats for ${website.id}", e)
                }
            }
            lastGuildCount.set(guildCount)
        } catch (e: Exception) {
            logger.error("Error executing schedule.", e)
        }
    }


    private fun getBotId(shardManager: ShardManager): String {
        return shardManager.shards.first().selfUser.id
    }

    private fun getShardGuildSize(shardManager: ShardManager): Int {
        return shardManager.shards.map { it.guildCache.size().toInt() }.reduce { acc, i -> acc + i }
    }

    fun shutdown() {
        task?.cancel(false)
        threadPoolExecutor.shutdown()
    }

    class Builder {

        private var shardManager: ShardManager? = null
        private val websites = mutableListOf<Website>()
        private var okHttpClient = OkHttpClient.Builder().build()
        private var threadPool: ScheduledExecutorService? = null
        private var initialDelay = 10L
        private var period = 30L
        private var timeUnit = TimeUnit.MINUTES

        fun setOkHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        fun withShardManager(shardManager: ShardManager): Builder {
            this.shardManager = shardManager
            return this
        }

        fun addWebsite(website: Website): Builder {
            if (websites.any { it.id == website.id || it.urlRegex == website.urlRegex })
                throw IllegalArgumentException("Website with id ${website.id} is already registered.")

            this.websites.add(website)
            return this
        }

        fun addWebsites(websites: Iterable<Website>): Builder {
            for (website in websites) {
                if (this.websites.any { it.id == website.id || it.urlRegex == website.urlRegex })
                    throw IllegalArgumentException("Website with id ${website.id} is already registered.")

                this.websites.add(website)
            }
            return this
        }

        fun setThreadPool(executorService: ScheduledExecutorService): Builder {
            this.threadPool = executorService
            return this
        }

        fun withInitialDelay(initialDelay: Long): Builder {
            if (initialDelay < 0L) throw IllegalArgumentException("initialDelay must be > 0")
            this.initialDelay = initialDelay
            return this
        }

        fun withRepetitionPeriod(period: Long): Builder {
            if (period < 0L) throw IllegalArgumentException("initialDelay must be > 0")
            this.period = period
            return this
        }

        fun withTimeUnit(timeUnit: TimeUnit): Builder {
            this.timeUnit = timeUnit
            return this
        }

        fun build(): StatsPosting {
            checkNotNull(this.shardManager) {
                "ShardManager must to be defined."
            }
            check(this.websites.isNotEmpty()) {
                "Websites cannot be null."
            }
            return StatsPosting(
                this.shardManager!!,
                this.okHttpClient,
                this.threadPool ?: Executors.newSingleThreadScheduledExecutor(),
                this.websites,
                initialDelay,
                period,
                timeUnit
            )
        }
    }

    companion object {
        internal val logger = LoggerFactory.getLogger(StatsPosting::class.java)
    }
}