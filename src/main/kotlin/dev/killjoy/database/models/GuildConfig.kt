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

import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object GuildConfigsTable : LongIdTable("guild_configs") {
    val lang = varchar("lang", 2).default("en")
}

class GuildConfig(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<GuildConfig>(GuildConfigsTable) {
        fun newDefault(guild: Guild): GuildConfig {
            val _lang = guild.locale
            return new(guild.idLong) {

            }
        }
    }

    var lang by GuildConfigsTable.lang
        private set

}