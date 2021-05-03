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
import dev.killjoy.database.enums.JoinPugStatus
import dev.killjoy.database.models.Pug
import dev.killjoy.database.models.PugsTable
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class PugsRepository(override val conn: DatabaseConnection) : IRepository {

    suspend fun findById(uuid: UUID) = newSuspendedTransaction(Dispatchers.IO, conn) {
        Pug.findById(uuid)
    }

    suspend fun findByGuild(guild: Guild) = newSuspendedTransaction(Dispatchers.IO, conn) {
        Pug.find { PugsTable.guildId eq guild.idLong and PugsTable.isActive }.firstOrNull()
    }

    suspend fun findByOwner(user: User) = newSuspendedTransaction(Dispatchers.IO, conn) {
        Pug.find { PugsTable.ownerId eq user.idLong and PugsTable.isActive }.firstOrNull()
    }

    suspend fun joinPug(guild: Guild, user: User): JoinPugStatus = newSuspendedTransaction(Dispatchers.IO, conn) {
        val activePug = findByGuild(guild)
            ?: return@newSuspendedTransaction JoinPugStatus.PugDoesNotExists

        JoinPugStatus.Joined
    }
}