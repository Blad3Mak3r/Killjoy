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

package tv.blademaker.killjoy.utils

import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.BotConfig
import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.apis.stats.StatsPosting
import tv.blademaker.killjoy.apis.stats.Website
import tv.blademaker.killjoy.prometheus.Prometheus
import java.util.concurrent.TimeUnit

object Services {

    fun enableListing() {
        val websites = mutableListOf<Website>()
        BotConfig.getOrNull<String>("listing.topgg")?.let {
            val website = Website("top.gg", "https://top.gg/api/bots/%s/stats", it)
            websites.add(website)
        }
        BotConfig.getOrNull<String>("listing.dbotsgg")?.let {
            val website = Website("discord.bots.gg", "https://discord.bots.gg/api/v1/bots/%s/stats", it, "guildCount")
            websites.add(website)
        }
        BotConfig.getOrNull<String>("listing.botsfordiscord")?.let {
            val website = Website("botsfordiscord.com", "https://botsfordiscord.com/api/bot/%s", it)
            websites.add(website)
        }
        BotConfig.getOrNull<String>("listing.dboats")?.let {
            val website = Website("discord.boats", "https://discord.boats/api/bot/%s", it)
            websites.add(website)
        }

        if (websites.isEmpty()) {
            logger.info("Listing is not enabled.")
            return
        }

        StatsPosting.Builder()
            .withShardManager(Launcher.shardManager)
            .addWebsites(websites)
            .withInitialDelay(1)
            .withRepetitionPeriod(30)
            .withTimeUnit(TimeUnit.MINUTES)
            .build()
    }

    fun enableMetrics() {
        if(BotConfig.getOrDefault("prometheus.enabled", false)) {
            Prometheus()
        } else {
            logger.info("Metrics not enabled.")
        }
    }

    private val logger = LoggerFactory.getLogger(Services::class.java)

}