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

package dev.killjoy.utils

import dev.killjoy.Credentials
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import org.slf4j.LoggerFactory

@Suppress("MemberVisibilityCanBePrivate")
data class ShardingStrategy(
    val automatic: Boolean = false,
    val nodeID: Int = 0,
    val totalShards: Int,
    val shardsPerNode: Int,
    val concurrency: Int? = null
) {

    val fistShard: Int
        get() = shardsPerNode * nodeID

    val lastShard: Int
        get() = (totalShards - 1).coerceAtMost((shardsPerNode * (nodeID + 1)) - 1)

    companion object {

        fun create(args: ApplicationArguments): ShardingStrategy {
            val totalShards = Credentials.getOrDefault("sharding.total_shards", 1)
            val shardsPerNode = Credentials.getOrDefault("sharding.shards_per_node", totalShards)
            val concurrency = Credentials.getOrNull<Int>("sharding.concurrency")

            return ShardingStrategy(args.automaticSharding, args.node, totalShards, shardsPerNode, concurrency)
        }

        fun DefaultShardManagerBuilder.setShardingStrategy(strategy: ShardingStrategy): DefaultShardManagerBuilder {

            if (strategy.automatic) {
                setShardsTotal(-1)

                logger.info("Applying automatic sharding.")
            } else {
                val firstShard = strategy.fistShard
                val lastShard = strategy.lastShard

                setShardsTotal(strategy.totalShards)
                setShards(firstShard, lastShard)
                setMaxReconnectDelay(32)

                logger.info("Applying sharding strategy --> NID:${strategy.nodeID} TS:${strategy.totalShards} SPN:${strategy.shardsPerNode} FS:${firstShard} LS:${lastShard}")
            }

            return this
        }

        private val logger = LoggerFactory.getLogger(ShardingStrategy::class.java)
    }

}
