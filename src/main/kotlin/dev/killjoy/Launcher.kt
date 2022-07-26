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
import com.xenomachina.argparser.ArgParser
import dev.killjoy.apis.memes.Meme
import dev.killjoy.apis.news.ValorantNew
import dev.killjoy.apis.riot.entities.RankedPlayerList
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.cache.RedisCache
import dev.killjoy.database.Database
import dev.killjoy.database.DatabaseConnection
import dev.killjoy.database.buildDatabaseConnection
import dev.killjoy.extensions.jda.isInt
import dev.killjoy.i18n.I18n
import dev.killjoy.interactions.InteractionsHandler
import dev.killjoy.interactions.InteractionsVerifier
import dev.killjoy.interactions.installDiscordInteractions
import dev.killjoy.prometheus.Prometheus
import dev.killjoy.utils.*
import dev.killjoy.valorant.agent.AgentAbility
import dev.killjoy.valorant.agent.ValorantAgent
import dev.killjoy.valorant.arsenal.ValorantWeapon
import dev.killjoy.valorant.map.ValorantMap
import dev.killjoy.webhook.WebhookUtils
import dev.kord.common.entity.DiscordApplication
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.sentry.Sentry
import net.dv8tion.jda.api.requests.RestAction
import net.hugebot.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
object Launcher : Killjoy {

    var application: DiscordApplication? = null
        private set

    lateinit var database: Database
        private set

    lateinit var cache: RedisCache
        private set

    var pid by Delegates.notNull<Long>()
        private set

    lateinit var agents: List<ValorantAgent>
        private set

    lateinit var arsenal: List<ValorantWeapon>
        private set

    lateinit var maps: List<ValorantMap>
        private set

    lateinit var server: NettyApplicationEngine
        private set

    lateinit var keyVerifier: InteractionsVerifier
        private set

    val rateLimiter: RateLimiter = RateLimiter.Builder()
        .setQuota(20)
        .setExpirationTime(1, TimeUnit.MINUTES)
        .build()

    private val RESTACTION_DEFAULT_FAILURE = RestAction.getDefaultFailure()

    @Throws(LoginException::class, ConfigException::class)
    fun init(args: ApplicationArguments) {
        pid = ProcessHandle.current().pid()

        Utils.printBanner(pid, log)

        //Initialize sentry
        SentryUtils.init()

        database = buildDatabaseConnection()

        cache = RedisCache.createSingleServer(
            {
                this.nettyThreads = 5
                this.threads = 1
            },
            {
                this.address = RedisCache.buildUrl()
                this.password = Credentials.getOrNull<String>("redis.pass")
                this.database = Credentials.getOrDefault("redis.db", 0)
                this.clientName = "killjoy-interactions"
            }
        )

        keyVerifier = InteractionsVerifier(Credentials["discord.publicKey"])

        val kord = RestClient(Credentials.token)

        val interactionsHandler = InteractionsHandler(kord)

        server = embeddedServer(Netty, host = "0.0.0.0", port = 2550) {
            routing {
                installDiscordInteractions(handler = interactionsHandler, Credentials.publicKey)
            }
        }

        I18n.init()

        // Load entities after banner
        agents = Loaders.loadAgents()
        arsenal = Loaders.loadArsenal()
        maps = Loaders.loadMaps()

        if (Credentials.getOrDefault("prometheus.enabled", false)) {
            Prometheus()
        }

        Credentials.getOrNull<String>("webhook_url")?.let { WebhookUtils.init(it) }

        Runtime.getRuntime().addShutdownHook(Thread {
            Thread.currentThread().name = "shutdown-thread"

            shutdown(0)
        })
    }

    @JvmStatic
    @Throws(LoginException::class, ConfigException::class)
    fun main(args: Array<String>) {
        val parsedArgs = ArgParser(args).parseInto(::ApplicationArguments)

        init(parsedArgs)
    }

    override fun shutdown(code: Int) {
        try {
            log.info("Shutting down Killjoy with code $code...")

            WebhookUtils.shutdown()
            HttpUtils.shutdown()

            cache.shutdown()
        } catch (e: Exception) {
            Sentry.captureException(e)
            log.error("Exception shutting down Killjoy.", e)
            Runtime.getRuntime().halt(code)
        }
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

    override suspend fun getLeaderboard(region: Region): RankedPlayerList {
        return cache.leaderboards.get(region)
    }

    override suspend fun getNews(locale: Locale): List<ValorantNew> {
        return cache.news.get(locale)
    }

    override suspend fun getMeme(subreddit: String): Meme {
        return cache.memes.get(subreddit)
    }

    suspend fun getApplicationId(kord: RestClient): Snowflake {
        if (application == null) {
            application = kord.application.getCurrentApplicationInfo()
        }

        return application!!.id
    }

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}