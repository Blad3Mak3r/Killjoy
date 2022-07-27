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
import dev.kord.common.entity.InteractionResponseType
import dev.kord.rest.service.RestClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class InteractionsHandler(private val rest: RestClient) {

    suspend fun onPing(call: ApplicationCall) {
        call.respondText(buildJsonObject {
            put("type", InteractionResponseType.Pong.type)
        }.toString(), ContentType.Application.Json)
    }

    suspend fun onCommand(call: ApplicationCall, interaction: DiscordInteraction) {
        val ctx = SlashCommandContext(call, rest, interaction)

        ctx.setEphemeral(true)
        ctx.respond("Interactions working as expected... ``${ctx.path}``\n```json\n${Json.encodeToString(interaction.data).trimIndent()}\n```")
    }

    suspend fun onComponent(call: ApplicationCall, interaction: DiscordInteraction) {

    }

    suspend fun onAutocomplete(call: ApplicationCall, interaction: DiscordInteraction) {

    }

    suspend fun onModalSubmit(call: ApplicationCall, interaction: DiscordInteraction) {

    }

}