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

package dev.killjoy.database.models

import net.dv8tion.jda.api.JDA
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object ShardStatsTable : IntIdTable("shard_stats") {
    val guildCount = integer("guild_count").default(0)
    val userCount = integer("user_count").default(0)
}

class ShardStats(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ShardStats>(ShardStatsTable) {
        fun new(shard: JDA) = new(shard.shardInfo.shardId) {
            this.guildCount = shard.guildCache.size().toInt()
            this.userCount = shard.guildCache.map { it.memberCount }.takeIf { it.isNotEmpty() }?.reduce { acc, i -> acc + i } ?: 0
        }
    }

    var guildCount by ShardStatsTable.guildCount
        private set

    var userCount by ShardStatsTable.userCount
        private set

    fun updateStats(shard: JDA) {
        this.guildCount = shard.guildCache.size().toInt()
        this.userCount = shard.guildCache.map { it.memberCount }.takeIf { it.isNotEmpty() }?.reduce { acc, i -> acc + i } ?: 0
    }
}