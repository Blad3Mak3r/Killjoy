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

import dev.killjoy.getConfig

typealias DatabaseConnection = org.jetbrains.exposed.sql.Database

private val LOCAL_ADDRESS_REGEX = "(localhost|(192|127|10)\\.(\\d){1,3}\\.(\\d){1,3}\\.(\\d))".toRegex()

internal fun discoverSslMode(host: String): String {
    return if (LOCAL_ADDRESS_REGEX matches host) "allow"
    else "require"
}

internal fun buildDatabaseConnection(): Database {
    val host = getConfig("host", "localhost")
    val port = getConfig("port", 5432)
    val user = getConfig("user", "killjoy")
    val password = getConfig("password", "killjoy")
    val name = getConfig("name", "killjoy")

    return Database(host, port, user, password, name)
}