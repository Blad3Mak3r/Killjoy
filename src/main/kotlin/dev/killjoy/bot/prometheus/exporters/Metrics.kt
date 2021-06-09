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

package dev.killjoy.bot.prometheus.exporters

import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import net.dv8tion.jda.api.JDA
import dev.killjoy.extensions.jda.shardIdString

object Metrics {

    private val COMMANDS_COUNTER: Gauge = Gauge.build()
        .name("killjoy_commands_counter")
        .help("Command ran by name")
        .labelNames("name")
        .register()

    private val RECEIVED_MESSAGE_EVENTS: Counter = Counter.build()
        .name("killjoy_message_events")
        .help("Message received events")
        .labelNames("shard")
        .register()

    private val RECEIVED_TOTAL_EVENTS: Counter = Counter.build()
        .name("killjoy_total_events")
        .help("Total received events")
        .labelNames("shard")
        .register()

    private val GUILD_COUNT: Gauge = Gauge.build()
        .name("killjoy_guild_count")
        .help("Guild count")
        .labelNames("shard")
        .register()

    fun increaseCommandUsage(command: String) {
        COMMANDS_COUNTER
            .labels(command.toLowerCase())
            .inc()
    }

    fun increaseMessageEvents(shard: JDA) {
        RECEIVED_MESSAGE_EVENTS
            .labels(shard.shardInfo.shardIdString)
            .inc()
    }

    fun increaseTotalEvents(shard: JDA) {
        RECEIVED_TOTAL_EVENTS
            .labels(shard.shardInfo.shardIdString)
            .inc()
    }

    fun updateShardStats(shard: JDA) {
        GUILD_COUNT
            .labels("${shard.shardInfo.shardId}")
            .set(shard.guildCache.size().toDouble())
    }
}