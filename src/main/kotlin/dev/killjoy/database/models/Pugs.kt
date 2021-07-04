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

import dev.killjoy.database.extensions.array
import dev.killjoy.i18n.i18nCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant
import java.util.*

object PugsTable : UUIDTable("pugs") {
    val guildId = long("guild_id")
    val ownerId = long("owner_id")
    val isActive = bool("is_active").default(true)
    val players = array<Long>("players", LongColumnType())
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

@Suppress("MemberVisibilityCanBePrivate")
class Pug(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Pug>(PugsTable) {
        fun newWithOwner(owner: User, guild: Guild) = new(null) {
            guildId = guild.idLong
            ownerId = owner.idLong
            players = arrayOf(owner.idLong)
        }
    }

    var guildId by PugsTable.guildId
        private set

    var ownerId by PugsTable.ownerId
        private set

    var isActive by PugsTable.isActive
        internal set

    var players by PugsTable.players
        internal set

    var createdAt by PugsTable.createdAt
        private set

    val taggedPlayers: List<String>
        get() = players.map { "<@$it>" }

    fun addPlayer(user: User): Boolean {
        if (players.size >= 12) return false
        val playersList = players.toMutableList()
        playersList.add(user.idLong)
        players = playersList.toTypedArray()
        return flush()
    }

    fun removePlayer(user: User): Boolean {
        val playersList = players.toMutableList()
        playersList.remove(user.idLong)
        players = playersList.toTypedArray()
        return flush()
    }

    fun close(): Boolean {
        isActive = false
        return flush()
    }

    fun asEmbed(guild: Guild): MessageEmbed {
        return EmbedBuilder().run {
            setColor(0xFF4753)
            setTitle(guild.i18nCommand("pugs.activeTitle"))
            addField(guild.i18nCommand("pugs.embedField", players.size), taggedPlayers.joinToString("\n"), false)
            setFooter("ID(${id})")
            setTimestamp(createdAt)
            build()
        }
    }
}