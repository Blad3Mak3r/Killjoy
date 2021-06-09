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
import dev.killjoy.database.enums.ClosePugResult
import dev.killjoy.database.enums.CreatePugResult
import dev.killjoy.database.enums.JoinPugResult
import dev.killjoy.database.enums.LeavePugResult
import dev.killjoy.database.models.Pug
import dev.killjoy.database.models.PugsTable
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class PugsRepository(override val conn: DatabaseConnection) : IRepository {

    suspend fun findById(uuid: UUID) = newSuspendedTransaction(Dispatchers.IO, conn) {
        Pug.findById(uuid)
    }

    suspend fun findByGuild(guild: Guild) = newSuspendedTransaction(Dispatchers.IO, conn) {
        Pug.find { PugsTable.guildId eq guild.idLong and PugsTable.isActive }
            .orderBy(Pair(PugsTable.createdAt, SortOrder.ASC))
            .firstOrNull()
    }

    suspend fun findByOwner(user: User) = newSuspendedTransaction(Dispatchers.IO, conn) {
        Pug.find { PugsTable.ownerId eq user.idLong and PugsTable.isActive }
            .orderBy(Pair(PugsTable.createdAt, SortOrder.ASC))
            .firstOrNull()
    }

    suspend fun create(guild: Guild, user: User): CreatePugResult = newSuspendedTransaction(Dispatchers.IO, conn) {
        val activePug = findByGuild(guild) != null

        if (activePug) return@newSuspendedTransaction CreatePugResult.AlreadyActivePug

        try {
            Pug.newWithOwner(user, guild)
            CreatePugResult.Opened
        } catch (e: Exception) {
            CreatePugResult.CantOpen
        }
    }

    suspend fun close(guild: Guild): ClosePugResult = newSuspendedTransaction(Dispatchers.IO, conn) {
        val activePug = findByGuild(guild)
            ?: return@newSuspendedTransaction ClosePugResult.NotActivePug

        val closed = try {
            activePug.close()
        } catch (e: Exception) {
            Sentry.captureException(e)
            false
        }

        if (closed) ClosePugResult.Closed
        else ClosePugResult.CantClose
    }

    suspend fun joinPug(guild: Guild, user: User): JoinPugResult = newSuspendedTransaction(Dispatchers.IO, conn) {
        val activePug = findByGuild(guild)
            ?: return@newSuspendedTransaction JoinPugResult.PugDoesNotExists

        if (activePug.players.contains(user.idLong)) return@newSuspendedTransaction JoinPugResult.AlreadyJoined

        val result = activePug.addPlayer(user)

        if (result) JoinPugResult.Joined
        else JoinPugResult.CantJoin
    }

    suspend fun leavePug(guild: Guild, user: User): LeavePugResult = newSuspendedTransaction(Dispatchers.IO, conn) {
        val activePug = findByGuild(guild)
            ?: return@newSuspendedTransaction LeavePugResult.PugDoesNotExists

        if (!activePug.players.contains(user.idLong)) return@newSuspendedTransaction LeavePugResult.AlreadyLeft

        val result = activePug.removePlayer(user)

        if (result) LeavePugResult.Left
        else LeavePugResult.CantLeft
    }
}