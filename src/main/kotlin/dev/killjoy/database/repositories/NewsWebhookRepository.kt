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

package dev.killjoy.database.repositories

import dev.killjoy.database.DatabaseConnection
import dev.killjoy.database.models.NewsWebhook
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Webhook
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.lang.IllegalStateException

class NewsWebhookRepository(override val conn: DatabaseConnection) : IRepository {

    suspend fun find(guild: Guild): NewsWebhook? {
        return newSuspendedTransaction(Dispatchers.IO, conn) {
            NewsWebhook.findById(guild.idLong)
        }
    }

    suspend fun create(guild: Guild, webhook: Webhook): NewsWebhook {
        if (webhook.token == null) throw IllegalStateException("Webhook token cannot be null.")

        return newSuspendedTransaction(Dispatchers.IO, conn) {
            NewsWebhook.new(guild, webhook.channel, webhook.idLong, webhook.token!!)
        }
    }

    suspend fun exists(guild: Guild) = newSuspendedTransaction(Dispatchers.IO, conn) {
        NewsWebhook.findById(guild.idLong) != null
    }

    suspend fun remove(hook: NewsWebhook) = newSuspendedTransaction(Dispatchers.IO, conn) {
        hook.delete()
    }
}