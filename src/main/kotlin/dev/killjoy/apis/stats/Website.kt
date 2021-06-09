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

package dev.killjoy.apis.stats

import com.typesafe.config.Config
import dev.killjoy.BotConfig.getOrDefault
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Website(
    val id: String,
    val urlRegex: String,
    private val token: String,
    private val entityName: String
) {

    constructor(config: Config) : this(
        config.getString("id"),
        config.getString("url"),
        config.getString("token"),
        config.getOrDefault("entity_type", "server_count")
    )

    internal fun postStats(httpClient: OkHttpClient, botId: String, guildCount: Int) {
        doRequest(httpClient, buildRequest(botId, guildCount))
    }

    private fun doRequest(httpClient: OkHttpClient, request: Request) {
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                StatsPosting.logger.error("Error executing call for $id", e)
            }

            override fun onResponse(call: Call, response: Response) {
                StatsPosting.logger.info("Stats for $id posted with response: [${response.code}] ${response.message}!")
                response.body?.close()
            }
        })
    }

    private fun buildRequest(botId: String, guildCount: Int) = Request.Builder()
        .url(String.format(this.urlRegex, botId))
        .addHeader("Authorization", this.token)
        .post(buildBody(this.entityName, guildCount))
        .build()

    private fun buildBody(entityName: String, guildCount: Int) = "{\"$entityName\": $guildCount}".toRequestBody(MEDIA_TYPE)

    companion object {
        private val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}