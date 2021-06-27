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

import dev.killjoy.Credentials
import dev.killjoy.database.models.NewsWebhooksTable
import dev.killjoy.database.models.PugsTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

typealias DatabaseConnection = org.jetbrains.exposed.sql.Database

private inline fun <reified T> getCredential(property: String, fallback: T): T =
    Credentials.getOrDefault("database.$property", fallback)

internal fun buildDatabaseConnection(): Database {
    val host = getCredential("host", "localhost")
    val port = getCredential("port", 5432)
    val user = getCredential("user", "killjoy")
    val password = getCredential("password", "killjoy")
    val name = getCredential("name", "killjoy")
    val synchronize = getCredential("synchronize", false)

    val db = Database(host, port, user, password, name)

    if (synchronize) transaction(db.connection) {
        SchemaUtils.createMissingTablesAndColumns(
            NewsWebhooksTable,
            PugsTable
        )
    }

    return db
}