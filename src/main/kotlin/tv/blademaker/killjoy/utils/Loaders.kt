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

package tv.blademaker.killjoy.utils

import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.apis.riot.entities.RankedPlayer
import tv.blademaker.killjoy.valorant.ValorantAgent
import tv.blademaker.killjoy.valorant.ValorantEntity
import tv.blademaker.killjoy.valorant.ValorantMap
import tv.blademaker.killjoy.valorant.ValorantWeapon
import java.io.InputStream
import kotlin.jvm.Throws
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


object Loaders {

    private val log = LoggerFactory.getLogger(Loaders::class.java)

    fun loadAgents(): List<ValorantAgent> = loadValorantEntities(ValorantAgent::class.java, "agents")
    fun loadArsenal(): List<ValorantWeapon> = loadValorantEntities(ValorantWeapon::class.java, "arsenal")
    fun loadMaps(): List<ValorantMap> = loadValorantEntities(ValorantMap::class.java, "maps")

    fun loadLeaderboards(): Map<String, List<RankedPlayer>> {
        val map = mutableMapOf<String, List<RankedPlayer>>()

        val path = "leaderboards"
        val index = getResourceFiles(path)

        check(index.isNotEmpty()) { "LEADERBOARDS index cannot be empty or null." }

        for (fileName in index) {
            val file = this::class.java.getResource("/$path/$fileName")
                ?: throw IllegalStateException("/$path/$fileName is not present")

            val fileContent = file.readText()
            if (fileContent.isEmpty()) throw IllegalStateException("$path/$fileName is empty")

            val region = fileName.toUpperCase().removeSuffix(".JSON")

            val players = JSONArray(fileContent).map {
                it as JSONObject
                RankedPlayer(
                    it.getString("puuid"),
                    it.getString("gameName"),
                    it.getString("tagLine"),
                    it.getInt("leaderboardRank"),
                    it.getInt("rankedRating"),
                    it.getInt("numberOfWins")
                )
            }.sortedBy { it.leaderboardRank }

            map[region] = players
        }

        log.info("Loaded ${map.size} region leaderboards!! [${map.keys.joinToString(", ")}]")

        return map
    }

    /**
     * Load a list of provided [ValorantEntity] based class.
     *
     * @param clazz a class extending the interface [ValorantEntity]
     * @param resourcePath the path to the resource (maps, agents, arsenal)
     *
     * @return a list of the given valorant entity [ValorantEntity]
     */
    @Throws(IllegalStateException::class)
    private fun <T : ValorantEntity> loadValorantEntities(clazz: Class<T>, resourcePath: String): List<T> {
        val list = mutableListOf<T>()
        val index = getResourceFiles(resourcePath)
        check(index.isNotEmpty()) { "${resourcePath.capitalize()} index cannot be empty or null." }

        for (entityName in index) {
            val file = this::class.java.getResource("/$resourcePath/$entityName")
                ?: throw IllegalStateException("/$resourcePath/$entityName is not present")

            val fileContent = file.readText()
            if (fileContent.isEmpty()) throw IllegalStateException("$resourcePath/$entityName is empty")
            list.add(clazz.getConstructor(JSONObject::class.java).newInstance(JSONObject(fileContent)))
        }

        log.info("Loaded ${list.size} ${clazz.simpleName} entities!! [${list.joinToString(", ") { it.name }}]")
        return list
    }

    @Throws(IOException::class)
    private fun getResourceFiles(path: String): List<String> {
        val fileNames = mutableListOf<String>()

        getResourceAsStream(path).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { br ->
                var resource: String?
                while (br.readLine().also { resource = it } != null) {
                    if (resource != null && resource!!.endsWith(".json")) fileNames.add(resource!!)
                }
            }
        }

        return fileNames
    }

    private fun getResourceAsStream(resource: String): InputStream {
        val inputStream = getContextClassLoader().getResourceAsStream(resource)

        return inputStream ?: this::class.java.getResourceAsStream(resource)
    }

    private fun getContextClassLoader(): ClassLoader {
        return Thread.currentThread().contextClassLoader
    }
}