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

package dev.killjoy.prometheus

import dev.killjoy.Credentials
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import org.slf4j.LoggerFactory
import java.io.StringWriter

class Prometheus {

    private val registry = CollectorRegistry.defaultRegistry
    private val engine: NettyApplicationEngine

    private val host = Credentials.getOrDefault("prometheus.host", "localhost")
    private val port = Credentials.getOrDefault("prometheus.port", 8080)

    init {
        logger.info("Starting Netty/Prometheus server...")
        engine = embeddedServer(Netty, port, host) {
            routing {
                get("/ping") {
                    call.respondText("Ok")
                }
                get("/") {
                    call.respondText(exposePrometheusMetrics())
                }
                get("/metrics") {
                    call.respondText(exposePrometheusMetrics())
                }
            }
        }

        engine.addShutdownHook {
            logger.info("Shutting down Netty/Prometheus server...")
            engine.stop(1000, 1000)
        }

        engine.start(false)

        DefaultExports.initialize()
    }

    private fun exposePrometheusMetrics(): String {
        val writer = StringWriter()
        TextFormat.write004(writer, registry.metricFamilySamples())
        return writer.toString()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Prometheus::class.java)
    }
}