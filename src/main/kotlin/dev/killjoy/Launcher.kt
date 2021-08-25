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
import dev.killjoy.database.Database
import dev.killjoy.database.DatabaseConnection
import dev.killjoy.database.buildDatabaseConnection
import dev.killjoy.extensions.jda.Intents
import dev.killjoy.extensions.jda.isInt
import dev.killjoy.framework.CommandRegistry
import dev.killjoy.i18n.I18n
import dev.killjoy.listeners.MainListener
import dev.killjoy.prometheus.Prometheus
import dev.killjoy.utils.*
import dev.killjoy.valorant.agent.AgentAbility
import dev.killjoy.valorant.agent.ValorantAgent
import dev.killjoy.valorant.arsenal.ValorantWeapon
import dev.killjoy.valorant.map.ValorantMap
import dev.killjoy.webhook.WebhookUtils
import dev.minn.jda.ktx.CoroutineEventManager
import dev.minn.jda.ktx.listener
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.hugebot.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.handler.DefaultSlashCommandHandler
import tv.blademaker.slash.api.handler.SlashCommandHandler
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.security.auth.login.LoginException
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
object Launcher : Killjoy {

    lateinit var database: Database
        private set

    private lateinit var shardManager: ShardManager

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

    private val RESTACTION_DEFAULT_FAILURE = RestAction.getDefaultFailure()

    @JvmStatic
    @Throws(LoginException::class, ConfigException::class)
    fun main(args: Array<String>) {

        pid = ProcessHandle.current().pid()

        RestAction.setPassContext(false)
        RestAction.setDefaultTimeout(7, TimeUnit.SECONDS)
        RestAction.setDefaultFailure {
            if (it !is TimeoutException) {
                RESTACTION_DEFAULT_FAILURE.accept(it)
            }
        }

        Utils.printBanner(pid, log)

        //Initialize sentry
        SentryUtils.init()

        database = buildDatabaseConnection()

        I18n.init()

        // Load entities after banner
        agents = Loaders.loadAgents()
        arsenal = Loaders.loadArsenal()
        maps = Loaders.loadMaps()

        commandRegistry = CommandRegistry()
        slashCommandHandler = DefaultSlashCommandHandler("dev.killjoy.commands")

        cooldownManager = CooldownManager(15, TimeUnit.SECONDS)

        if (Credentials.getOrDefault("prometheus.enabled", false)) {
            Prometheus()
        }

        Credentials.getOrNull<String>("webhook_url")?.let { WebhookUtils.init(it) }

        shardManager = DefaultShardManagerBuilder.createLight(Credentials.token)
            .setShardsTotal(-1)
            .setActivityProvider { Activity.competing("Valorant /help") }
            .setEventManagerProvider {
                val executor = Utils.newCoroutineDispatcher("event-manager-worker-%d", 4, 20, 6L, TimeUnit.MINUTES)
                CoroutineEventManager(CoroutineScope(executor + SupervisorJob()))
            }
            .setCompression(Compression.ZLIB)
            .disableIntents(Intents.allIntents)
            .enableIntents(Intents.enabledIntents)
            .enableCache(CacheFlag.MEMBER_OVERRIDES)
            .disableCache(
                CacheFlag.VOICE_STATE,
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ONLINE_STATUS,
                CacheFlag.ROLE_TAGS
            )
            .setBulkDeleteSplittingEnabled(false)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setMemberCachePolicy(MemberCachePolicy.NONE)
            .build()

        shardManager.listener<GenericEvent> { event ->
            when (event) {
                is SlashCommandEvent -> slashCommandHandler.onSlashCommandEvent(event)
                else -> MainListener.onEvent(event)
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            Thread.currentThread().name = "shutdown-thread"

            shutdown(0)
        })
    }

    override fun shutdown(code: Int) {
        try {
            log.info("Shutting down Killjoy with code $code...")

            commandRegistry.shutdown()
            WebhookUtils.shutdown()
            HttpUtils.shutdown()

            for (jda in shardManager.shardCache) {
                shardManager.shutdown(jda.shardInfo.shardId)
            }

            shardManager.shutdown()
        } catch (e: Exception) {
            Sentry.captureException(e)
            log.error("Exception shutting down Killjoy.", e)
            Runtime.getRuntime().halt(code)
        }
    }

    override fun getShardManager(): ShardManager {
        return shardManager
    }

    override fun getDatabaseConnection(): DatabaseConnection {
        return database.connection
    }

    override fun getAgent(input: String): ValorantAgent? {
        return if (input.isInt()) agents.find { it.number == input.toInt() }
        else agents.find { it.name.equals(input, true) }
    }

    override fun getWeapon(name: String): ValorantWeapon? {
        return arsenal.find { it.names.any { w -> w.equals(name, true) } }
    }

    override fun getWeaponById(id: String): ValorantWeapon? {
        return arsenal.find { it.ids.any { _id -> _id.equals(id,true) } }
    }

    override fun getMap(name: String): ValorantMap? {
        return maps.find { it.name.equals(name, true) }
    }

    override fun getAbilities(agentName: String): List<AgentAbility> {
        return getAgent(agentName)?.abilities ?: emptyList()
    }

    override fun getAbilities(): List<AgentAbility> {
        return agents.map { it.abilities }.reduce { acc, list -> acc + list }
    }

    override fun getAbility(name: String): AgentAbility? {
        val n = name.lowercase()
        return getAbilities().find {
            it.name.containsValue(n) || it.name.containsValue(n.replace("_", " "))
        }
    }

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}