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

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object AccountConnectionTable : LongIdTable("account_connections") {
    val puuid = varchar("puuid", 255)
    val username = varchar("username", 255)
    val gameTag = varchar("game_tag", 255)
}

class AccountConnection(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<AccountConnection>(AccountConnectionTable)

    val puuid by AccountConnectionTable.puuid
    val username by AccountConnectionTable.username
    val gameTag by AccountConnectionTable.gameTag

}

data class AccountWithStats(
    val account: AccountConnection,
    val stats: PlayerStats? = null
)