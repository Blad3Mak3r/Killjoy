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
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.service.RestClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicBoolean

class SlashCommandContext(
    private val call: ApplicationCall,
    private val rest: RestClient,
    val interaction: DiscordInteraction
) {

    private val ack = AtomicBoolean(false)
    private val ephemeral = AtomicBoolean(false)

    private val isAcknowledged = ack.get()

    fun setEphemeral(ephemeral: Boolean) {
        this.ephemeral.set(ephemeral)
    }

    suspend fun acknowledge() {
        if (!isAcknowledged) {
            call.respondText(buildJsonObject {
                put("type", InteractionResponseType.DeferredChannelMessageWithSource.type)
                if (ephemeral.get()) put("flags", 64)
            }.toString(), ContentType.Application.Json)
        }
    }

    suspend fun respond(content: String) {
        if (isAcknowledged) {
            rest.interaction.createFollowupMessage(interaction.applicationId, interaction.token) {
                this.content = content
            }
        } else {
            call.respondText(buildJsonObject {
                put("type", InteractionResponseType.ChannelMessageWithSource.type)
                put("data", buildJsonObject {
                    put("content", content)
                    if (ephemeral.get()) put("flags", 64)
                })
            }.toString(), ContentType.Application.Json)
        }
    }

    suspend fun respondEmbed(embed: EmbedBuilder.() -> Unit) {
        if (isAcknowledged) {
            rest.interaction.createFollowupMessage(interaction.applicationId, interaction.token) {
                this.embed(embed)
            }
        } else {
            val dEmbed = Json.encodeToJsonElement(EmbedBuilder().apply(embed).toRequest()).jsonObject
            call.respondText(buildJsonObject {
                put("type", InteractionResponseType.ChannelMessageWithSource.type)
                put("data", buildJsonObject {
                    put("embeds", buildJsonArray {
                        add(dEmbed)
                    })
                })
            }.toString(), ContentType.Application.Json)
        }
    }

}