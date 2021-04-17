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

package tv.blademaker.killjoy.apis.stats

import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.sharding.ShardManager

class ReadyListener(private val shardManager: ShardManager, private val statsPosting: StatsPosting) : ListenerAdapter() {

    private var ready = 0
    private val totalShards = shardManager.shardsTotal

    override fun onReady(event: ReadyEvent) {
        ready++

        if (ready == totalShards) {
            statsPosting.task = statsPosting.createTask()
            shardManager.removeEventListener(this)
        }
    }

}