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

import dev.killjoy.Launcher
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.Webhook
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

object PostedNewsTable : UUIDTable("news_posted") {
    val uid = varchar("uid", 255)
    val title = varchar("title", 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

class PostedNew(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PostedNew>(PostedNewsTable) {
        fun new(id: UUID, uid: String, title: String) = new(id) {
            this.uid = uid
            this.title = title
        }
    }

    var uid by PostedNewsTable.uid
        private set

    var title by PostedNewsTable.title
        private set

    val createdAt by PostedNewsTable.createdAt
}

object NewsWebhooksTable : LongIdTable("news_webhooks", "guild_id") {
    val channelId = long("channel_id")
    val hookID = long("hook_id")
    val hookToken = varchar("hook_token", 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

class NewsWebhook(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<NewsWebhook>(NewsWebhooksTable) {
        fun new(guild: Guild, channel: TextChannel, id: Long, token: String) = new(guild.idLong) {
            channelId = channel.idLong
            hookID = id
            hookToken = token
        }
    }

    var channelId by NewsWebhooksTable.channelId
        private set

    var hookID by NewsWebhooksTable.hookID
        private set

    var hookToken by NewsWebhooksTable.hookToken
        private set

    val url: String
        get() = "https://discord.com/api/webhooks/$hookID/$hookToken"

    val createdAt by NewsWebhooksTable.createdAt

    suspend fun update(newId: Long, newToken: String): Boolean {
        return newSuspendedTransaction(Dispatchers.IO, Launcher.database.connection) {
            hookID = newId
            hookToken = newToken
            flush()
        }
    }
}