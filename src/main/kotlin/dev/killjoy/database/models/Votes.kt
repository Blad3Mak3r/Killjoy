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

import dev.killjoy.rest.models.VoteHook
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object VotesTable : LongIdTable("votes") {
    val total = integer("total").default(1)
    val first = timestamp("first").defaultExpression(CurrentTimestamp())
    val last = timestamp("last").defaultExpression(CurrentTimestamp())
}

class Vote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(VotesTable) {
        fun new(id: Long) = new(id) {
            total = 0
            last = Instant.now()
        }
    }

    var total by VotesTable.total
        private set

    val first by VotesTable.first

    var last by VotesTable.last
        private set

    fun upvote(): Boolean {
        total += 1
        last = Instant.now()
        return flush()
    }
}