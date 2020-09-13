package tv.blademaker.killjoy.core

import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory

data class Agent (
    val name: String,
    val bio: String,
    val role: Role,
    val avatar: String,
    val skills: List<Skill>
) {

    constructor(json: JSONObject) : this(
        json.getString("name"),
        json.getString("bio"),
        Role.of(json.getString("role")),
        json.getString("avatar"),
        Skill.ofAll(json.getJSONArray("skills"))
    )

    enum class Role {
        Controller,
        Duelist,
        Initiator,
        Sentinel;

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
        val info: String,
        val preview: String
    ) {

        constructor(json: JSONObject) : this(
            Button.of(json.getString("button")),
            json.getString("name"),
            json.getString("icon"),
            json.getString("info"),
            json.getString("preview")
        )

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