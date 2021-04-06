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
import org.reflections.Reflections
import org.reflections.ReflectionsException
import org.reflections.scanners.ResourcesScanner
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.apis.riot.entities.RankedPlayer
import tv.blademaker.killjoy.valorant.ValorantAgent
import tv.blademaker.killjoy.valorant.ValorantEntity
import tv.blademaker.killjoy.valorant.ValorantMap
import tv.blademaker.killjoy.valorant.ValorantWeapon
import java.io.InputStream
import kotlin.jvm.Throws
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path


object Loaders {

    private val log = LoggerFactory.getLogger(Loaders::class.java)

    fun loadAgents(): List<ValorantAgent> = loadValorantEntities(ValorantAgent::class.java, "agents")
    fun loadArsenal(): List<ValorantWeapon> = loadValorantEntities(ValorantWeapon::class.java, "arsenal")
    fun loadMaps(): List<ValorantMap> = loadValorantEntities(ValorantMap::class.java, "maps")

    fun loadLeaderboards(): Map<String, List<RankedPlayer>> {
        val map = mutableMapOf<String, List<RankedPlayer>>()

        val path = "leaderboards"
        val index = this::class.java.getResource("/$path/index").readText().split("\\r?\\n".toRegex())

        check(index.isNotEmpty()) { "LEADERBOARDS index cannot be empty or null." }

        for (fileName in index) {
            val file = this::class.java.getResource("/$path/$fileName.json")
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
     * @param clazz a class extending the interface [ValorantEntity].
     * @param resourcePath the path to the resource (maps, agents, arsenal).
     *
     * @throws ReflectionsException When the provided resourcePath not exists.
     * @throws IllegalStateException When some of the resources from the resourcePath not exists or is empty.
     *
     * @return a list of the given valorant entity [ValorantEntity].
     */
    @Throws(IllegalStateException::class, ReflectionsException::class)
    private fun <T : ValorantEntity> loadValorantEntities(clazz: Class<T>, resourcePath: String): List<T> {
        val entities = mutableListOf<T>()

        val indexes = Reflections(resourcePath, ResourcesScanner())
            .getResources(".*\\.json".toPattern())

        for (index in indexes) {
            val file = this::class.java.getResource("/$index")
                ?: throw IllegalStateException("/$index is not present")

            val content = file.readText()
            if (content.isEmpty()) throw IllegalStateException("/$index is empty")
            entities.add(clazz.getConstructor(JSONObject::class.java).newInstance(JSONObject(content)))
        }

        log.info("Loaded ${entities.size} ${clazz.simpleName} entities!! [${entities.joinToString(", ") { it.name }}]")
        return entities
    }
}