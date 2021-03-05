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

package tv.blademaker.killjoy.apis.riot

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import kong.unirest.Cache
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.apis.riot.entities.RankedPlayer
import tv.blademaker.killjoy.apis.riot.entities.Region
import tv.blademaker.killjoy.valorant.ValorantEntity
import java.util.concurrent.TimeUnit

object RiotAPI {

    private val enabled: Boolean
        get() = apiKey != null

    private var apiKey: String? = null

    fun init(apiKey: String?) {
        if (apiKey == null) return
        this.apiKey = apiKey
    }

    private val leaderboardsCache = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build<String, List<RankedPlayer>>()

    suspend fun getCurrentTop20(region: Region): List<RankedPlayer> {

        val cached = leaderboardsCache.getIfPresent(region.name.toUpperCase())
        if (cached != null) return cached

        logger.info("Retrieving fresh leaderboards for ${region.name.toUpperCase()}")

        val url = "https://dgxfkpkb4zk5c.cloudfront.net/leaderboards/affinity/${region.name.toUpperCase()}/queue/competitive/act/$CURRENT_ACT_ID?startIndex=0&size=10"
        val r = Unirest.get(url).asJsonAsync().await()

        check(r.isSuccess) { "Not success status: ${r.status}" }

        val list = r.body.`object`.getJSONArray("players").map {
            it as JSONObject
            RankedPlayer(
                it.getString("puuid"),
                it.getString("gameName"),
                it.getString("tagLine"),
                it.getInt("leaderboardRank"),
                it.getInt("rankedRating"),
                it.getInt("numberOfWins")
            )
        }.sortedBy { p -> p.leaderboardRank }

        leaderboardsCache.put(region.name.toUpperCase(), list)
        return list
    }

    suspend fun getTop20(region: Region, size: Int = 20, index: Int = 0): List<RankedPlayer> {
        check(enabled) { "RiotAPI is not initialized. Use RiotAPI.init(apiKey)" }

        val url = VAL_RANKED_V1
            .replace("{region}", region.name.toLowerCase())
            .replace("{size}", "$size")
            .replace("{index}", "$index")

        val r = Unirest.get(url).header("X-Riot-Token", apiKey).asJsonAsync().await()

        check(r.isSuccess) { "Not success status: ${r.status}" }

        return r.body.`object`.getJSONArray("players").map {
            it as JSONObject
            RankedPlayer(
                it.getString("puuid"),
                it.getString("gameName"),
                it.getString("tagLine"),
                it.getInt("leaderboardRank"),
                it.getInt("rankedRating"),
                it.getInt("numberOfWins")
            )
        }.sortedBy { p -> p.leaderboardRank }
    }


    private const val CURRENT_ACT_ID = "ab57ef51-4e59-da91-cc8d-51a5a2b9b8ff"   // Episode 1 (Act 1)
    private const val VAL_RANKED_V1 = "https://{region}.api.riotgames.com/val/ranked/v1/leaderboards/by-act/$CURRENT_ACT_ID?size={size}&startIndex={index}"
    private val logger = LoggerFactory.getLogger(RiotAPI::class.java)
}