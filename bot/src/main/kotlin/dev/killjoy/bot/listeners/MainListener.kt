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

package dev.killjoy.bot.listeners

import dev.killjoy.bot.Launcher
import dev.killjoy.bot.prometheus.exporters.Metrics
import dev.killjoy.bot.utils.Utils
import io.sentry.Sentry
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class MainListener : EventListener {
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

    private fun onGuildJoin(event: GuildJoinEvent) = handleEvent {
        if (event.guild.selfMember.timeJoined.isBefore(OffsetDateTime.now().minusSeconds(30)))
            return@handleEvent

        logger.info("Joined Guild: ${event.guild.name}::${event.guild.id} with a total of ${event.guild.memberCount} members.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onGuildLeave(event: GuildLeaveEvent) = handleEvent {
        logger.info("Left Guild: ${event.guild.name}::${event.guild.id}.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onGuildMessageReceived(event: GuildMessageReceivedEvent) = handleEvent {
        Launcher.commandRegistry.onGuildMessageReceived(event)
        Metrics.increaseMessageEvents(event.jda)
    }

    /**
     * Shards events
     */

    private fun onReady(event: ReadyEvent) = handleEvent {
        logger.info("JDA Shard#${event.jda.shardInfo.shardId} ready with ${event.guildAvailableCount} guilds available(s) of ${event.guildTotalCount}.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onReconnected(event: ReconnectedEvent) = handleEvent {
        logger.info("JDA Shard#${event.jda.shardInfo.shardId} has reconnected.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onResumed(event: ResumedEvent) = handleEvent {
        logger.info("JDA Shard#${event.jda.shardInfo.shardId} has resumed.")

        Metrics.updateShardStats(event.jda)
    }

    private fun onDisconnect(event: DisconnectEvent) = handleEvent {
        if (event.isClosedByServer) {
            logger.warn("JDA Shard#${event.jda.shardInfo.shardId} disconnected (server-side). Code: ${event.serviceCloseFrame?.closeCode ?: -1} ${event.closeCode}")
        } else {
            logger.warn("JDA Shard#${event.jda.shardInfo.shardId} disconnected. Code: ${event.serviceCloseFrame?.closeCode ?: -1} ${event.closeCode}")
        }
    }

    private fun onException(event: ExceptionEvent) = handleEvent {
        Sentry.captureException(event.cause)
        if (!event.isLogged)
            logger.error("Exception in JDA {}", event.jda.shardInfo.shardId, event.cause)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MainListener::class.java)

        //Handle events in a separate ThreadPool
        private val EVENT_POOL = Utils.newThreadFactory("jda-event-pool-worker-%d", 4, 20, 6L, TimeUnit.MINUTES)
        private fun handleEvent(runnable: Runnable) {
            EVENT_POOL.execute {
                synchronized(this) {
                    try {
                        runnable.run()
                    } catch (e: Exception) {
                        logger.error("Exception handling event.", e)
                    }
                }
            }
        }
    }
}