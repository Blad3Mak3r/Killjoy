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

@file:Suppress("unused")

package tv.blademaker.killjoy.valorant

import net.dv8tion.jda.api.EmbedBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.Launcher
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.utils.extensions.isUrl
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@Suppress("unused")
data class ValorantAgent (
    val id: String? = null,
    val number: Int,
    private val apiName: String? = null,
    override val name: String,
    val bio: String,
    val origin: String,
    val role: Role,
    val avatar: String,
    val thumbnail: String,
    val skills: List<Skill>
) : ValorantEntity {

    private var stats: Stats? = null

    fun getStats(): Stats {
        if (StatsMapper.lastCheck.get() == 0L || StatsMapper.lastCheck.get() < System.currentTimeMillis()) StatsMapper.doUpdate()

        return stats ?: Stats.default()
    }

    constructor(json: JSONObject) : this(
        json.getString("id").takeIf { it.isNotEmpty() },
        json.getInt("number"),
        json.optString("api_name").trim().takeIf { it.isNotEmpty() },
        json.getString("name").trim(),
        json.getString("bio").trim(),
        json.getString("origin").trim(),
        Role.of(json.getString("role")),
        json.getString("avatar"),
        json.getString("thumbnail"),
        Skill.ofAll(json.getJSONArray("skills"))
    )

    init {
        check(this.avatar.isUrl()) { "avatar is not a valid url (ValorantAgent ${this.name}) [${this.avatar}]" }
    }

    fun asEmbed(): EmbedBuilder {
        return EmbedBuilder().apply {
            setAuthor(role.name, null, role.iconUrl)
            setTitle(name, "https://playvalorant.com/en-us/agents/${name.toLowerCase()}/")
            setThumbnail(avatar)
            //setImage(thumbnail)
            setDescription(bio)
            addField("Origin", origin, true)
            addField("Win Ratio", String.format("%.2f", getStats().winRatio), true)
            addField("KDA Ratio", String.format("%.2f", getStats().kdaRatio), true)
            addField("AVG Damage", String.format("%.2f", getStats().avgDamage), true)
            addField("AVG Kills", String.format("%.2f", getStats().avgKills), true)
            addField("AVG Assists", String.format("%.2f", getStats().avgAssists), true)
            addBlankField(false)
            setColor(ColorExtra.VAL_RED)
            for (skill in skills) {
                addField("`` ${skill.button.name.toUpperCase()} `` - ${skill.name}", skill.info, false)
            }
        }
    }

    enum class Role(val emoji: String, val iconUrl: String) {
        Controller("<:controller:754676227809214485>", "https://i.imgur.com/V4Ci1Oh.png"),
        Duelist("<:duelist:754676227952083025>", "https://i.imgur.com/rs0d2qx.png"),
        Initiator("<:initiator:754676227582722062>", "https://i.imgur.com/hCVcqgf.png"),
        Sentinel("<:sentinel:754676227994026044>", "https://i.imgur.com/ODX86kl.png");

        val snowFlake: String
            get() = emoji.removeSuffix("<").removePrefix(">")

        companion object {
            fun of(str: String): Role {
                return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid role name.")
            }
        }
    }

    data class Stats(
        val avgDamage: Double,
        val avgKills: Double,
        val avgAssists: Double,
        val winRatio: Double,
        val kdaRatio: Double
    ) {

        constructor(json: JSONObject) : this(
            json.getDouble("average_damage"),
            json.getDouble("average_kills"),
            json.getDouble("average_assists"),
            json.getDouble("win_ratio"),
            json.getDouble("kda_ratio")
        )

        companion object {
            fun default() = Stats(00.00, 00.00, 00.00, 00.00, 00.00)
        }
    }

    data class Skill(
        val button: Button,
        val name: String,
        val iconUrl: String,
        val info: String,
        val preview: String,
        val cost: String = ""
    ) {

        constructor(json: JSONObject) : this(
            Button.of(json.getString("button")),
            json.getString("name").trim(),
            json.getString("iconUrl").trim(),
            json.getString("info").trim(),
            json.getString("preview").trim(),
            kotlin.runCatching { json.getString("cost") }.getOrDefault("")
        )

        init {
            check(this.iconUrl.isUrl()) { "iconUrl is not a valid URL. (Skill $name) [${this.iconUrl}]" }
            check(this.preview.isUrl()) { "preview is not a valid URL. (Skill $name) [${this.preview}]" }
        }

        val id: String
            get() = buildIdentifier(name)

        enum class Button {
            Q,
            E,
            C,
            X;

            companion object {
                fun of(str: String): Button {
                    return values().find { it.name.equals(str, true) } ?: throw IllegalArgumentException("$str is not a valid role name.")
                }
            }
        }

        companion object {
            fun ofAll(array: JSONArray) = array.map { it as JSONObject }.map { Skill(it) }

            private fun buildIdentifier(str: String): String {
                return str.trim().toLowerCase().replace(" ", "").replace("’", "").replace("'", "").replace("\"", "")
            }
        }
    }

    object StatsMapper {
        var lastCheck: AtomicLong = AtomicLong(0L)
        private const val url = "https://valorantics-ow.kda.gg/tierlist/agent"
        private val logger = LoggerFactory.getLogger(StatsMapper::class.java)

        fun doUpdate() {
            val r = Request.Builder().apply {
                url(url)
                addHeader("User-ValorantAgent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
            }.build()
            Launcher.httpClient.newCall(r).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.warn("Cannot update stats")
                    lastCheck.set(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2))
                }

                override fun onResponse(call: Call, response: Response) {
                    var count = 0
                    lastCheck.set(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30))

                    logger.info("Last check was ${lastCheck.get()}")

                    val content = response.body?.string() ?: return logger.warn("Cannot update stats [Empty body]")
                    val stats = JSONObject(content).getJSONArray("all")

                    Launcher.agents.forEach { agent ->
                        try {
                            val agentStats = stats.find {
                                val jsonAgent = it as JSONObject
                                jsonAgent.getString("name") == "${agent.apiName}_pc_c"
                            } as JSONObject

                            agent.stats = Stats(agentStats)
                            count++
                        } catch (e: Throwable) {
                            logger.error("Cannot update agent stats => ${agent.name}\n${e.stackTraceToString()}")
                        }
                    }

                    response.close()

                    logger.info("Loaded $count stats for agents.")
                }

            })
        }
    }
}