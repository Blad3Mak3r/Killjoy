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

package dev.killjoy.rest

import dev.killjoy.Credentials
import dev.killjoy.Launcher
import dev.killjoy.rest.models.DefaultResponse
import dev.killjoy.rest.models.VoteHook
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean

object Spike {

    private val logger = LoggerFactory.getLogger(Spike::class.java)
    private val registry = CollectorRegistry.defaultRegistry
    private val engine: NettyApplicationEngine

    private val host = Credentials.getOrDefault("spike.host", "localhost")
    private val port = Credentials.getOrDefault("spike.port", 8080)
    private val enablePrometheus = Credentials.getOrDefault("spike.prometheus", false)
    private val enableAPI = Credentials.getOrDefault("spike.api", false)

    private val isStarted = AtomicBoolean(false)

    init {
        engine = embeddedServer(Netty, port, host) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                get("/") {
                    val response = DefaultResponse(enableAPI, enablePrometheus)
                    call.respond(response)
                }
            }
        }
    }

    private fun enableAPI() {
        engine.application.routing {
            get("/api") {
                call.respondText("API enabled")
            }
            post("/vote") {
                val vote = call.receive<VoteHook>()
                logger.info(vote.toString())
                Launcher.database.vote.upvote(vote)
                call.respondText("Ok")
            }
        }
    }

    private fun enablePrometheus() {
        engine.application.routing {
            get("/metrics") {
                call.respondText(exposePrometheusMetrics())
            }
        }
        DefaultExports.initialize()
    }

    fun start() {
        if (!isStarted.compareAndSet(false, true))
            throw IllegalStateException("Spike is already started.")

        logger.info("Starting Spike service.")
        engine.start(false)

        if (enablePrometheus) enablePrometheus()
        if (enableAPI) enableAPI()

        engine.addShutdownHook {
            logger.info("Shutting down Netty/Prometheus server...")
            engine.stop(1000, 1000)
        }
    }

    private fun exposePrometheusMetrics(): String {
        val writer = StringWriter()
        TextFormat.write004(writer, registry.metricFamilySamples())
        return writer.toString()
    }
}