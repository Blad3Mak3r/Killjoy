package tv.blademaker.killjoy

import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.sharding.ShardManager
import net.hugebot.ratelimiter.RateLimiter
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.core.Agent
import java.util.concurrent.TimeUnit

object Launcher {

    lateinit var shardManager: ShardManager
        private set

    lateinit var config: Any
        private set

    lateinit var info: ApplicationInfo
        private set

    lateinit var agents: List<Agent>
        private set

    val rateLimiter: RateLimiter = RateLimiter.Builder().setQuota(20).setExpirationTime(1, TimeUnit.MINUTES).build()

    @JvmStatic
    fun main(args: Array<String>) {

        agents = Loaders.loadAgents()


    }

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}