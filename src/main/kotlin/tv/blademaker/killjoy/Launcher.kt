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

package tv.blademaker.killjoy

import com.typesafe.config.ConfigException
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.hugebot.ratelimiter.RateLimiter
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.apis.stats.StatsPosting
import tv.blademaker.killjoy.apis.stats.Website
import tv.blademaker.killjoy.framework.CommandRegistry
import tv.blademaker.killjoy.listeners.MainListener
import tv.blademaker.killjoy.prometheus.Prometheus
import tv.blademaker.slash.api.handler.DefaultSlashCommandHandler
import tv.blademaker.slash.api.handler.SlashCommandHandler
import tv.blademaker.killjoy.utils.CooldownManager
import tv.blademaker.killjoy.utils.Loaders
import tv.blademaker.killjoy.utils.SentryUtils
import tv.blademaker.killjoy.utils.Utils
import tv.blademaker.killjoy.utils.extensions.isInt
import tv.blademaker.killjoy.valorant.ValorantAgent
import tv.blademaker.killjoy.valorant.ValorantMap
import tv.blademaker.killjoy.valorant.ValorantWeapon
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.properties.Delegates

object Launcher {

    val httpClient: OkHttpClient = OkHttpClient()

    lateinit var shardManager: ShardManager
        private set

    var pid by Delegates.notNull<Long>()
        private set

    lateinit var config: Any
        private set

    lateinit var info: ApplicationInfo
        private set

    lateinit var commandRegistry: CommandRegistry
        private set

    lateinit var slashCommandHandler: SlashCommandHandler
        private set

    lateinit var cooldownManager: CooldownManager
        private set

    val agents: List<ValorantAgent> = Loaders.loadAgents()
    val arsenal: List<ValorantWeapon> = Loaders.loadArsenal()
    val maps: List<ValorantMap> = Loaders.loadMaps()
    //val leaderboards = Loaders.loadLeaderboards()

    val rateLimiter: RateLimiter = RateLimiter.Builder().setQuota(20).setExpirationTime(1, TimeUnit.MINUTES).build()

    @JvmStatic
    @Throws(LoginException::class, ConfigException::class)
    fun main(args: Array<String>) {

        pid = ProcessHandle.current().pid()

        log.info(Utils.printBanner(pid))

        //Initialize sentry
        SentryUtils.init()

        //Waiting for Riot approval
        // - RiotAPI.init(BotConfig.getOrNull<String>("riot.api_key"))

        commandRegistry = CommandRegistry()
        slashCommandHandler = DefaultSlashCommandHandler("tv.blademaker.killjoy.commands")

        cooldownManager = CooldownManager(15, TimeUnit.SECONDS)

        if (BotConfig.getOrDefault("prometheus.enabled", false)) {
            Prometheus()
        }

        shardManager = DefaultShardManagerBuilder.createLight(BotConfig.token)
            .setShardsTotal(-1)
            .setActivity(Activity.competing("Valorant | joy help"))
            .setEnableShutdownHook(false)
            .addEventListeners(MainListener(slashCommandHandler))
            .setCompression(Compression.ZLIB)
            .setEnableShutdownHook(true)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES
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

        enableListing()
    }

    /**
     * Get agent by name or number.
     *
     * @param input Agent name or number formatted as [String].
     *
     * @return A nullable [ValorantAgent]
     */
    fun getAgent(input: String): ValorantAgent? {
        return if (input.isInt()) agents.find { it.number == input.toInt() }
        else agents.find { it.name.equals(input, true) }
    }

    fun getWeapon(name: String) = arsenal.find { it.name.equals(name, true) }
    fun getWeaponById(id: String) = arsenal.find { it.id.equals(id, true) }

    fun getMap(name: String) = maps.find { it.name.equals(name, true) }

    fun getAgentsByRole(name: String): List<ValorantAgent>? {
        val result = kotlin.runCatching { ValorantAgent.Role.of(name) }
        if (result.isFailure) return null
        return agents.filter { it.role === result.getOrNull()!! }
    }

    fun getAbilities() = agents.map { it.abilities }.reduce { acc, list -> acc + list }

    fun getSkills() = agents.map { it.skills }.reduce { acc, list -> acc + list }

    private fun enableListing() {
        try {
            val websites = BotConfig.getNullableConfigList("listing")?.map { Website(it) }

            if (websites == null || websites.isEmpty()) {
                log.info("Listing is not enabled.")
            } else {
                StatsPosting.Builder()
                    .withShardManager(shardManager)
                    .addWebsites(websites)
                    .withInitialDelay(1)
                    .withRepetitionPeriod(30)
                    .withTimeUnit(TimeUnit.MINUTES)
                    .build()
            }
        } catch (e: Exception) {
            log.error("Listing is not enabled.", e)
        }
    }

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}