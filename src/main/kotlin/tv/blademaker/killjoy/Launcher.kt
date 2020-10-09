package tv.blademaker.killjoy

import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.hugebot.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.framework.CommandRegistry
import tv.blademaker.killjoy.utils.Loaders
import tv.blademaker.killjoy.valorant.Agent
import tv.blademaker.killjoy.valorant.Weapon
import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Launcher {

    val scheduler = Executors.newSingleThreadScheduledExecutor()

    lateinit var shardManager: ShardManager
        private set

    lateinit var config: Any
        private set

    lateinit var info: ApplicationInfo
        private set

    lateinit var commandRegistry: CommandRegistry
        private set

    lateinit var agents: List<Agent>
    lateinit var arsenal: List<Weapon>

    val rateLimiter: RateLimiter = RateLimiter.Builder().setQuota(20).setExpirationTime(1, TimeUnit.MINUTES).build()

    @JvmStatic
    fun main(args: Array<String>) {

        log.info("Starting with PID: ${ManagementFactory.getRuntimeMXBean().pid}")

        try {
            agents = Loaders.loadAgents()
            arsenal = Loaders.loadArsenal()
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(0)
        }

        commandRegistry = CommandRegistry()

        shardManager = DefaultShardManagerBuilder.createLight(System.getenv("TOKEN"))
            .setActivity(Activity.playing("Valorant | joy help"))
            .setEnableShutdownHook(false)
            .addEventListeners(commandRegistry)
            .setCompression(Compression.ZLIB)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
            )
            .disableIntents(
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_PRESENCES
            )
            .enableCache(CacheFlag.MEMBER_OVERRIDES)
            .disableCache(
                CacheFlag.VOICE_STATE,
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS
            )
            .build()

    }

    fun getAgent(name: String) = agents.find { it.name.equals(name, true) }

    fun getWeapon(name: String) = arsenal.find { it.name.equals(name, true) }

    fun getWeaponById(id: String) = arsenal.find { it.id.equals(id, true) }

    fun getAgentsByRole(name: String): List<Agent>? {
        val result = kotlin.runCatching { Agent.Role.of(name) }
        if (result.isFailure) return null
        return agents.filter { it.role === result.getOrNull()!! }
    }

    fun getSkills() = agents.map { it.skills }.reduce { acc, list -> acc + list }

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}