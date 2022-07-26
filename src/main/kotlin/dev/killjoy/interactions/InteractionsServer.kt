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

package dev.killjoy.interactions

import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.InteractionType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

val JSON = Json {
    ignoreUnknownKeys = true
}

fun Routing.installDiscordInteractions(handler: InteractionsHandler, publicKey: String, path: String = "/interactions") {

    val verifier = InteractionsVerifier(publicKey)

    val log = KotlinLogging.logger {  }

    post(path) {
        val signature = call.request.header("X-Signature-Ed25519")
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "X-Signature-Ed25519 header not present.")
        val timestamp = call.request.header("X-Signature-Timestamp")
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "X-Signature-Timestamp header not present.")

        log.debug { "Signature Header: $signature" }
        log.debug { "Timestamp Header: $timestamp" }

        val text = withContext(Dispatchers.IO) {
            call.receiveStream().bufferedReader(charset = Charsets.UTF_8).readText()
        }

        log.debug { "Payload: $text" }

        val parsed = JSON.parseToJsonElement(text).jsonObject

        val type = parsed["type"]!!.jsonPrimitive.int

        log.debug { "Type: $type" }
        log.debug { "Checking signature..." }

        val verified = verifier.verifyKey(text, signature, timestamp)

        if (!verified) {
            call.respondText("", ContentType.Application.Json, HttpStatusCode.Unauthorized)
            return@post
        }

        log.debug { parsed }

        when (type) {
            InteractionType.Ping.type -> handler.onPing(call)
            InteractionType.ApplicationCommand.type -> {
                val interaction = JSON.decodeFromString<DiscordInteraction>(text)
                handler.onCommand(call, interaction)
            }
            InteractionType.Component.type -> {
                val interaction = JSON.decodeFromString<DiscordInteraction>(text)
                handler.onComponent(call, interaction)
            }
            InteractionType.AutoComplete.type -> {
                val interaction = JSON.decodeFromString<DiscordInteraction>(text)
                handler.onAutocomplete(call, interaction)
            }
            InteractionType.ModalSubmit.type -> {
                val interaction = JSON.decodeFromString<DiscordInteraction>(text)
                handler.onModalSubmit(call, interaction)
            }
        }
    }

}