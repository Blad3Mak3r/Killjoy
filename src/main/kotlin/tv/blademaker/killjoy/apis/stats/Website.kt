package tv.blademaker.killjoy.apis.stats

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.sharding.ShardManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Website(
    val id: String,
    val urlRegex: String,
    private val token: String,
    private val entityName: String = "server_count"
) {

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