@file:Suppress("unused")

package tv.blademaker.killjoy.valorant

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.json.JSONArray
import org.json.JSONObject
import tv.blademaker.killjoy.framework.ColorExtra

data class Agent (
    val name: String,
    val bio: String,
    val role: Role,
    val avatar: String,
    val thumbnail: String,
    val skills: List<Skill>
) {

    constructor(json: JSONObject) : this(
        json.getString("name"),
        json.getString("bio"),
        Role.of(json.getString("role")),
        json.getString("avatar"),
        json.getString("thumbnail"),
        Skill.ofAll(json.getJSONArray("skills"))
    )

    fun asEmbed(): EmbedBuilder {
        return EmbedBuilder().apply {
            setAuthor(role.name, null, role.iconUrl)
            setTitle(name, "https://playvalorant.com/en-us/agents/${name.toLowerCase()}/")
            setThumbnail(avatar)
            //setImage(thumbnail)
            setDescription(bio)
            addField("Pick Rate", "Coming soon...", true)
            addField("Win Rate", "Coming soon...", true)
            addBlankField(false)
            setColor(ColorExtra.VAL_RED)
            for (skill in skills) {
                addField("${skill.icon} [`` ${skill.button.name.toUpperCase()} ``] - ${skill.name}", skill.info, false)
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

    data class Skill(
        val button: Button,
        val name: String,
        val icon: String,
        val iconUrl: String,
        val info: String,
        val preview: String,
        val cost: String = ""
    ) {

        constructor(json: JSONObject) : this(
            Button.of(json.getString("button")),
            json.getString("name").trim(),
            json.getString("icon").trim(),
            json.getString("iconUrl").trim(),
            json.getString("info").trim(),
            json.getString("preview").trim(),
            kotlin.runCatching { json.getString("cost") }.getOrDefault("")
        )

        val id: String
            get() = name.toLowerCase()
                .replace(" ", "")
                .replace("’", "")
                .replace("'", "")

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
        }
    }
}