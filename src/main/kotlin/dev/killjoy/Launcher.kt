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

import dev.killjoy.apis.memes.Meme
import dev.killjoy.apis.news.ValorantNew
import dev.killjoy.apis.riot.entities.RankedPlayerList
import dev.killjoy.apis.riot.entities.Region
import dev.killjoy.cache.RedisCache
import dev.killjoy.database.Database
import dev.killjoy.database.DatabaseConnection
import dev.killjoy.extensions.isInt
import dev.killjoy.i18n.I18n
import dev.killjoy.interactions.InteractionsHandler
import dev.killjoy.interactions.InteractionsVerifier
import dev.killjoy.interactions.installDiscordInteractions
import dev.killjoy.prometheus.installPrometheus
import dev.killjoy.utils.*
import dev.killjoy.valorant.agent.AgentAbility
import dev.killjoy.valorant.agent.ValorantAgent
import dev.killjoy.valorant.arsenal.ValorantWeapon
import dev.killjoy.valorant.map.ValorantMap
import dev.killjoy.webhook.WebhookUtils
import dev.kord.rest.service.RestClient
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.sentry.Sentry
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.properties.Delegates

private val log = LoggerFactory.getLogger("Main")

private lateinit var database: Database

private lateinit var cache: RedisCache

var pid by Delegates.notNull<Long>()
    private set

private lateinit var agents: List<ValorantAgent>

private lateinit var arsenal: List<ValorantWeapon>

private lateinit var maps: List<ValorantMap>

private lateinit var server: NettyApplicationEngine

private lateinit var keyVerifier: InteractionsVerifier

fun main(/*_args: Array<String>*/) {
    //val args = ArgParser(_args).parseInto(::ApplicationArguments)

    pid = ProcessHandle.current().pid()
    Utils.printBanner(pid, log)

    SentryUtils.init()

    /*cache = RedisCache.createSingleServer(
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
        )*/

    keyVerifier = InteractionsVerifier(Credentials["discord.publicKey"])

    val kord = RestClient(Credentials.token)

    val interactionsHandler = InteractionsHandler(kord)

    server = embeddedServer(Netty, host = "0.0.0.0", port = 2550) {
        routing {
            installDiscordInteractions(handler = interactionsHandler, Credentials.publicKey)
            installPrometheus()
        }
    }

    I18n.init()

    // Load entities after banner
    agents = Loaders.loadAgents()
    arsenal = Loaders.loadArsenal()
    maps = Loaders.loadMaps()

    Credentials.getOrNull<String>("webhook_url")?.let { WebhookUtils.init(it) }

    Runtime.getRuntime().addShutdownHook(Thread {
        Thread.currentThread().name = "shutdown-thread"

        shutdown(0)
    })

    server.start(true)
}

private fun shutdown(code: Int) {
    try {
        log.info("Shutting down Killjoy with code $code...")

        WebhookUtils.shutdown()
        HttpUtils.shutdown()

        server.stop()
        //cache.shutdown()
    } catch (e: Exception) {
        Sentry.captureException(e)
        log.error("Exception shutting down Killjoy.", e)
        Runtime.getRuntime().halt(code)
    }
}

/*@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
object Launcher : Killjoy {

    @Throws(LoginException::class, ConfigException::class)
    fun init(args: ApplicationArguments) {


        //Initialize sentry

        //database = buildDatabaseConnection()

        /*cache = RedisCache.createSingleServer(
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
        )*/

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

        server.start(true)
    }

    @JvmStatic
    @Throws(LoginException::class, ConfigException::class)
    fun main(args: Array<String>) {
        val parsedArgs = ArgParser(args).parseInto(::ApplicationArguments)

        init(parsedArgs)
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

    private val log = LoggerFactory.getLogger(Launcher::class.java)
}
*/

fun getAgent(input: String): ValorantAgent? {
    return if (input.isInt) agents.find { it.number == input.toInt() }
    else agents.find { it.name.equals(input, true) }
}

fun getWeapon(name: String): ValorantWeapon? {
    return arsenal.find { it.names.any { w -> w.equals(name, true) } }
}

fun getMap(name: String): ValorantMap? {
    return maps.find { it.name.equals(name, true) }
}

fun getAbilities(agentName: String): List<AgentAbility> {
    return getAgent(agentName)?.abilities ?: emptyList()
}

fun getAbilities(): List<AgentAbility> {
    return agents.map { it.abilities }.reduce { acc, list -> acc + list }
}

fun getAbility(name: String): AgentAbility? {
    val n = name.lowercase()
    return getAbilities().find {
        it.name.containsValue(n) || it.name.containsValue(n.replace("_", " "))
    }
}

suspend fun getLeaderboard(region: Region): RankedPlayerList {
    return cache.leaderboards.get(region)
}

suspend fun getNews(locale: Locale): List<ValorantNew> {
    return cache.news.get(locale)
}

suspend fun getMeme(subreddit: String): Meme {
    return cache.memes.get(subreddit)
}

fun getAgents() = agents

fun getArsenal() = arsenal

fun getCache() = cache

fun getDatabase() = database

fun getMaps() = maps