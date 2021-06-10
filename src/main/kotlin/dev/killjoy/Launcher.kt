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

package dev.killjoy

import com.typesafe.config.ConfigException
import dev.killjoy.apis.stats.StatsPosting
import dev.killjoy.apis.stats.Website
import dev.killjoy.database.Database
import dev.killjoy.framework.CommandRegistry
import dev.killjoy.listeners.MainListener
import dev.killjoy.prometheus.Prometheus
import dev.killjoy.slash.api.handler.DefaultSlashCommandHandler
import dev.killjoy.slash.api.handler.SlashCommandHandler
import dev.killjoy.utils.CooldownManager
import dev.killjoy.utils.Loaders
import dev.killjoy.utils.SentryUtils
import dev.killjoy.utils.Utils
import dev.killjoy.utils.extensions.isInt
import dev.killjoy.valorant.ValorantAgent
import dev.killjoy.valorant.ValorantMap
import dev.killjoy.valorant.ValorantWeapon
import dev.killjoy.webhook.WebhookUtils
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.hugebot.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
object Launcher {

    lateinit var database: Database
        private set

    lateinit var shardManager: ShardManager
        private set

    var pid by Delegates.notNull<Long>()
        private set

    lateinit var info: ApplicationInfo
        private set

    lateinit var commandRegistry: CommandRegistry
        private set

    lateinit var slashCommandHandler: SlashCommandHandler
        private set

    lateinit var cooldownManager: CooldownManager
        private set

    lateinit var agents: List<ValorantAgent>
        private set

    lateinit var arsenal: List<ValorantWeapon>
        private set

    lateinit var maps: List<ValorantMap>
        private set

    val rateLimiter: RateLimiter = RateLimiter.Builder()
        .setQuota(20)
        .setExpirationTime(1, TimeUnit.MINUTES)
        .build()

    @JvmStatic
    @Throws(LoginException::class, ConfigException::class)
    fun main(args: Array<String>) {

        pid = ProcessHandle.current().pid()

        log.info(Utils.printBanner(pid))

        //Initialize sentry
        SentryUtils.init()

        database = buildDatabaseConnection()

        // Load entities after banner
        agents = Loaders.loadAgents()
        arsenal = Loaders.loadArsenal()
        maps = Loaders.loadMaps()

        commandRegistry = CommandRegistry()
        slashCommandHandler = DefaultSlashCommandHandler("dev.killjoy.bot.commands")

        cooldownManager = CooldownManager(15, TimeUnit.SECONDS)

        if (BotConfig.getOrDefault("prometheus.enabled", false)) {
            Prometheus()
        }

        BotConfig.getOrNull<String>("webhook_url")?.let { WebhookUtils.init(it) }

        shardManager = DefaultShardManagerBuilder.createLight(BotConfig.token)
            .setShardsTotal(-1)
            .setActivity(Activity.competing("Valorant | joy help"))
            .setEnableShutdownHook(false)
            .addEventListeners(
                MainListener(),
                slashCommandHandler
            )
            .setEventPool(Utils.newThreadFactory("jda-event-worker-%d", 4, 20, 6L, TimeUnit.MINUTES))
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

    private fun buildDatabaseConnection(): Database {
        val host = getConfig("host", "localhost")
        val port = getConfig("port", 5432)
        val user = getConfig("user", "killjoy")
        val password = getConfig("password", "killjoy")
        val name = getConfig("name", "killjoy")

        return Database(host, port, user, password, name)
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

    fun getAbilities() = agents.map { it.abilities }.reduce { acc, list -> acc + list }
    fun getAbility(name: String) = getAbilities().find { it.skill.name.equals(name, true) }

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