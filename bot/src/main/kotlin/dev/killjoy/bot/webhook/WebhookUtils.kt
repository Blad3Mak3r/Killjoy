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

package dev.killjoy.bot.webhook

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.WebhookCluster
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.events.ReadyEvent
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.atomic.AtomicBoolean

object WebhookUtils {

    private val logger = LoggerFactory.getLogger(WebhookUtils::class.java)

    private var client: WebhookClient? = null

    private val isEnabled = AtomicBoolean(false)

    fun init(url: String) {
        check(isEnabled.compareAndSet(false, true)) { "WebhookClient is already initialized." }

        try {
            logger.info("Initializing webhook utils and client with url: $url")
            client = WebhookClient.withUrl(url)
        } catch (e: Exception) {
            client = null
            isEnabled.set(false)
            logger.error("Error enabling webhook client.", e)
        }
    }

    @Synchronized
    fun shutdown() {
        logger.info("Shutting down webhook utils and client.")
        client?.close()
        client = null
        isEnabled.set(false)
    }


    fun send(content: String) {
        client?.send(content)
            ?.thenAccept { m ->
                logger.info("Webhook was sent successfully (${m.id})")
            }
            ?.exceptionally { t->
                logger.error("Cannot send webhook.", t)
                null
            }
    }

    fun sendShardReady(event: ReadyEvent) = send {
        val shardID = event.jda.shardInfo.shardId
        val totalShards = event.jda.shardManager!!.shardsTotal
        setAuthor(WebhookEmbed.EmbedAuthor("[ $shardID / $totalShards ]", event.jda.selfUser.effectiveAvatarUrl, null))
        setTitle(WebhookEmbed.EmbedTitle("Shard ready", null))
        addField(WebhookEmbed.EmbedField(true, "Total Guilds", "${event.guildTotalCount}"))
        addField(WebhookEmbed.EmbedField(true, "Available", "${event.guildAvailableCount}"))
        addField(WebhookEmbed.EmbedField(true, "Unavailable", "${event.guildUnavailableCount}"))
        setColor(0x19c815)
    }

    fun sendJoinGuild(guild: Guild) = send {
        setAuthor(WebhookEmbed.EmbedAuthor("Joined new Guild", guild.jda.selfUser.effectiveAvatarUrl, null))
        setColor(0x00b4ff)
        setTitle(WebhookEmbed.EmbedTitle(guild.name, null))
        setThumbnailUrl(guild.iconUrl)
        addField(WebhookEmbed.EmbedField(true, "Members", "${guild.memberCount}"))
    }

    fun sendLeaveGuild(guild: Guild) = send {
        setAuthor(WebhookEmbed.EmbedAuthor("Left Guild", guild.jda.selfUser.effectiveAvatarUrl, null))
        setColor(0xc81000)
        setTitle(WebhookEmbed.EmbedTitle(guild.name, null))
        setThumbnailUrl(guild.iconUrl)
        addField(WebhookEmbed.EmbedField(true, "Members", "${guild.memberCount}"))
    }

    fun send(embedBuilder: WebhookEmbedBuilder.() -> Unit) {
        client?.send(WebhookEmbedBuilder().apply(embedBuilder).build())
            ?.whenComplete { m, t ->
                if (t != null) logger.error("Error sending webhook.", t)
                logger.info("Webhook has been sent successfully (${m.id})")
            }
    }

}