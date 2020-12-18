package tv.blademaker.killjoy.apis.stats

import net.dv8tion.jda.api.sharding.ShardManager
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.concurrent.*

class StatsPosting private constructor(
    private val shardManager: ShardManager,
    internal val httpClient: OkHttpClient,
    private val threadPoolExecutor: ScheduledExecutorService,
    private val websites: List<Website>,
    private val initialDelay: Long,
    private val period: Long,
    private val timeUnit: TimeUnit
) {

    private val task: ScheduledFuture<*>

    init {
        logger.info("Enabled guild count for ${websites.joinToString(", ") { it.id }}!")
        task = threadPoolExecutor.scheduleAtFixedRate(schedule(), initialDelay, period, timeUnit)
    }

    private fun schedule(): Runnable {
        return Runnable {
            try {
                for (website in websites) {
                    try {
                        website.postStats(httpClient, shardManager)
                    } catch (e: Exception) {
                        logger.error("Cannot post stats for ${website.id}", e)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error executing schedule.", e)
            }
        }
    }

    fun shutdown() {
        task.cancel(false)
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
                if (websites.any { it.id == website.id || it.urlRegex == website.urlRegex })
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
            if (this.shardManager == null) throw IllegalStateException("ShardManager must to be defined.")
            if (this.websites.isEmpty()) throw IllegalStateException("Websites cannot be null.")
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