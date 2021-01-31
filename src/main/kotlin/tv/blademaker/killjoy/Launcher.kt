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
import tv.blademaker.killjoy.apis.riot.RiotAPI
import tv.blademaker.killjoy.apis.stats.StatsPosting
import tv.blademaker.killjoy.apis.stats.Website
import tv.blademaker.killjoy.framework.CommandRegistry
import tv.blademaker.killjoy.utils.CooldownManager
import tv.blademaker.killjoy.utils.Loaders
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

        log.info("Starting with PID: $pid")

        //Waiting for Riot approval
        // - RiotAPI.init(BotConfig.getOrNull<String>("riot.api_key"))

        commandRegistry = CommandRegistry()

        cooldownManager = CooldownManager(15, TimeUnit.SECONDS)

        shardManager = DefaultShardManagerBuilder.createLight(BotConfig.token)
            .setShardsTotal(-1)
            .setActivity(Activity.competing("Valorant | joy help"))
            .setEnableShutdownHook(false)
            .addEventListeners(commandRegistry)
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

    fun retrieveAgentByInput(input: String): ValorantAgent? {
        return if (input.isInt()) getAgent(input.toInt())
        else getAgent(input)
    }

    fun getAgent(number: Int) = agents.find { it.number == number }
    fun getAgent(name: String) = agents.find { it.name.equals(name, true) }

    fun getWeapon(name: String) = arsenal.find { it.name.equals(name, true) }
    fun getWeaponById(id: String) = arsenal.find { it.id.equals(id, true) }

    fun getMap(name: String) = maps.find { it.name.equals(name, true) }

    fun getAgentsByRole(name: String): List<ValorantAgent>? {
        val result = kotlin.runCatching { ValorantAgent.Role.of(name) }
        if (result.isFailure) return null
        return agents.filter { it.role === result.getOrNull()!! }
    }

    fun getSkills() = agents.map { it.skills }.reduce { acc, list -> acc + list }

    private fun enableListing() {
        val websites = mutableListOf<Website>()
        BotConfig.getOrNull<String>("listing.topgg")?.let {
            val website = Website("top.gg", "https://top.gg/api/bots/%s/stats", it)
            websites.add(website)
        }
        BotConfig.getOrNull<String>("listing.dbotsgg")?.let {
            val website = Website("discord.bots.gg", "https://discord.bots.gg/api/v1/bots/%s/stats", it, "guildCount")
            websites.add(website)
        }
        BotConfig.getOrNull<String>("listing.botsfordiscord")?.let {
            val website = Website("botsfordiscord.com", "https://botsfordiscord.com/api/bot/%s", it)
            websites.add(website)
        }
        BotConfig.getOrNull<String>("listing.dboats")?.let {
            val website = Website("discord.boats", "https://discord.boats/api/bot/%s", it)
            websites.add(website)
        }

        if (websites.isEmpty()) {
            log.info("Listing is not enabled.")
            return
        }

        StatsPosting.Builder()
            .withShardManager(shardManager)
            .addWebsites(websites)
            .withInitialDelay(1)
            .withRepetitionPeriod(30)
            .withTimeUnit(TimeUnit.MINUTES)
            .build()
    }

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}