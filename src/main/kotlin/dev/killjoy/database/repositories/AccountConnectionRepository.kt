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
import dev.killjoy.database.models.AccountConnection
import dev.killjoy.database.models.AccountWithStats
import dev.killjoy.database.models.PlayerStats
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

class AccountConnectionRepository(override val conn: DatabaseConnection) : IRepository {

    suspend fun findByUserAsync(user: User) = suspendedTransactionAsync(Dispatchers.IO, conn) {
        val acc = AccountConnection.findById(user.idLong)
            ?: return@suspendedTransactionAsync null

        val stats = PlayerStats.findById(acc.puuid)

        AccountWithStats(acc, stats)
    }

}