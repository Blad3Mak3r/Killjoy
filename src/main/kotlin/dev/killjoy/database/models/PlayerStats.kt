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

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class PuuidIdTable(name: String = "", columnName: String = "id") : IdTable<String>(name) {
    override val id: Column<EntityID<String>> = varchar(columnName, 255).entityId()
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

abstract class PuuidEntity(id: EntityID<String>) : Entity<String>(id)

abstract class PuuidEntityClass<out E : PuuidEntity>(table: IdTable<String>, entityType: Class<E>? = null) : EntityClass<String, E>(table, entityType)

object PlayerStatsTable : PuuidIdTable("player_stats") {

    val mostPlayedAgent = varchar("most_played_agent", 25).nullable()

    val mmr = integer("mmr").nullable()
    val rankTier = integer("rank_tier").nullable()

    val wins = integer("wins").nullable()
    val loses = integer("loses").nullable()

    val rankedWins = integer("ranked_wins").nullable()
    val rankedLoses = integer("ranked_loses").nullable()

    val kills = integer("kills").nullable()
    val deaths = integer("deaths").nullable()
    val assists = integer("assists").nullable()

    val rankedKills = integer("ranked_kills").nullable()
    val rankedDeaths = integer("ranked_deaths").nullable()
    val rankedAssists = integer("ranked_assists").nullable()

}

class PlayerStats(id: EntityID<String>) : PuuidEntity(id) {

    companion object : PuuidEntityClass<PlayerStats>(PlayerStatsTable)

    val mostPlayedAgent by PlayerStatsTable.mostPlayedAgent

    val mmr by PlayerStatsTable.mmr
    val rankTier by PlayerStatsTable.rankTier

    val wins by PlayerStatsTable.wins
    val loses by PlayerStatsTable.loses

    val rankedWins by PlayerStatsTable.rankedWins
    val rankedLoses by PlayerStatsTable.rankedLoses

    val kills by PlayerStatsTable.kills
    val deaths by PlayerStatsTable.deaths
    val assists by PlayerStatsTable.assists

    val rankedKills by PlayerStatsTable.rankedKills
    val rankedDeaths by PlayerStatsTable.rankedDeaths
    val rankedAssists by PlayerStatsTable.rankedAssists
}