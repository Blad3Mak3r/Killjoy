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

package dev.killjoy.database

import dev.killjoy.database.models.PugsTable
import dev.killjoy.getConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

typealias DatabaseConnection = org.jetbrains.exposed.sql.Database

internal fun buildDatabaseConnection(synchronize: Boolean): Database {
    val host = getConfig("host", "localhost")
    val port = getConfig("port", 5432)
    val user = getConfig("user", "killjoy")
    val password = getConfig("password", "killjoy")
    val name = getConfig("name", "killjoy")

    val db = Database(host, port, user, password, name)

    if (synchronize) transaction(db.connection) {
        SchemaUtils.createMissingTablesAndColumns(
            PugsTable
        )
    }

    return db
}