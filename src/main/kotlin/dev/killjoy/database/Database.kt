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

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.killjoy.Credentials
import dev.killjoy.database.models.ShardStats
import dev.killjoy.database.repositories.PugsRepository
import dev.killjoy.utils.Scopes
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class Database(
    private val host: String = "localhost",
    private val port: Int = 5432,
    private val user: String,
    private val password: String,
    private val name: String = "killjoy"
) {

    internal val connection: DatabaseConnection

    val pugs: PugsRepository

    init {
        logger.info("Initializing database connection...")

        val config = HikariConfig().apply {
            minimumIdle = 2
            maximumPoolSize = 12
            connectionTimeout = 10000

            dataSourceClassName = "com.impossibl.postgres.jdbc.PGDataSource"

            addDataSourceProperty("sslMode", "allow")
            addDataSourceProperty("serverName", this@Database.host)
            if (port > 0) addDataSourceProperty("portNumber", this@Database.port)
            addDataSourceProperty("user", this@Database.user)
            addDataSourceProperty("password", this@Database.password)
            addDataSourceProperty("databaseName", this@Database.name)
        }

        connection = DatabaseConnection.connect(HikariDataSource(config))

        pugs = PugsRepository(connection)
    }

    fun postShardStats(shard: JDA) {
        val isEnabled = Credentials.getOrDefault("sharding.stats", false)
        if (!isEnabled) return

        val shardID = shard.shardInfo.shardId

        Scopes.IO.launch {
            try {
                newSuspendedTransaction {
                    ShardStats.findById(shardID)
                        ?.updateStats(shard)
                        ?: ShardStats.new(shard)
                }
                logger.info("Updated stats for shard#${shardID}!")
            } catch (e: Exception) {
                logger.error("Cannot post shard stats for shard#${shardID}", e)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Database::class.java)
    }
}