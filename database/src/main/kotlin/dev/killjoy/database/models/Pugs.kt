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
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object PugsTable : UUIDTable("pugs") {
    val guildId = long("guild_id")
    val ownerId = long("owner_id")
    val isActive = bool("active").default(true)
}

class Pug(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Pug>(PugsTable) {
        fun newWithOwner(owner: User, guild: Guild) = new(UUID.randomUUID()) {
            guildId = guild.idLong
            ownerId = owner.idLong
        }
    }

    var guildId by PugsTable.guildId
        private set

    var ownerId by PugsTable.ownerId
        private set

    var isActive by PugsTable.isActive
        internal set
}