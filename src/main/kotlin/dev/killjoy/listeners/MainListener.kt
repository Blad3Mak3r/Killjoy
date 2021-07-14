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

package dev.killjoy.listeners

import dev.killjoy.Launcher
import dev.killjoy.prometheus.exporters.Metrics
import dev.killjoy.webhook.WebhookUtils
import io.sentry.Sentry
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime

object MainListener : EventListener {
    override fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildJoinEvent -> onGuildJoin(event)
            is GuildLeaveEvent -> onGuildLeave(event)

            is GuildMessageReceivedEvent -> onGuildMessageReceived(event)

            is ReadyEvent -> onReady(event)
            is DisconnectEvent -> onDisconnect(event)
            is ResumedEvent -> onResumed(event)
            is ReconnectedEvent -> onReconnected(event)

            is ExceptionEvent -> onException(event)
        }

        Metrics.increaseTotalEvents(event.jda)
    }

    /**
     * Guild events
     */

    private fun onGuildJoin(event: GuildJoinEvent) {
        if (event.guild.selfMember.timeJoined.isBefore(OffsetDateTime.now().minusSeconds(30)))
            return

        logger.info("Joined Guild: ${event.guild.name}::${event.guild.id} with a total of ${event.guild.memberCount} members.")
        WebhookUtils.sendJoinGuild(event.guild)
        Launcher.database.postShardStats(event.jda)
        Metrics.updateShardStats(event.jda)
    }

    private fun onGuildLeave(event: GuildLeaveEvent) {
        logger.info("Left Guild: ${event.guild.name}::${event.guild.id}.")
        WebhookUtils.sendLeaveGuild(event.guild)
        Launcher.database.postShardStats(event.jda)
        Metrics.updateShardStats(event.jda)
    }

    private fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        Launcher.commandRegistry.onGuildMessageReceived(event)
        Metrics.increaseMessageEvents(event.jda)
    }

    /**
     * Shards events
     */

    private fun onReady(event: ReadyEvent) {
        logger.info("JDA Shard#${event.jda.shardInfo.shardId} ready with ${event.guildAvailableCount} guilds available(s) of ${event.guildTotalCount}.")

        WebhookUtils.sendShardReady(event)
        Metrics.updateShardStats(event.jda)
        Launcher.database.postShardStats(event.jda)
    }

    private fun onReconnected(event: ReconnectedEvent) {
        logger.info("JDA Shard#${event.jda.shardInfo.shardId} has reconnected.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onResumed(event: ResumedEvent) {
        logger.info("JDA Shard#${event.jda.shardInfo.shardId} has resumed.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onDisconnect(event: DisconnectEvent) {
        if (event.isClosedByServer) {
            logger.warn("JDA Shard#${event.jda.shardInfo.shardId} disconnected (server-side). Code: ${event.serviceCloseFrame?.closeCode ?: -1} ${event.closeCode}")
        } else {
            logger.warn("JDA Shard#${event.jda.shardInfo.shardId} disconnected. Code: ${event.serviceCloseFrame?.closeCode ?: -1} ${event.closeCode}")
        }
    }

    private fun onException(event: ExceptionEvent) {
        Sentry.captureException(event.cause)
        Launcher.database.postShardStats(event.jda)
        if (!event.isLogged)
            logger.error("Exception in JDA {}", event.jda.shardInfo.shardId, event.cause)
    }


    private val logger = LoggerFactory.getLogger(MainListener::class.java)
}