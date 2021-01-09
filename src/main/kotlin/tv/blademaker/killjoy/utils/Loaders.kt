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

import org.json.JSONObject
import org.slf4j.LoggerFactory
import tv.blademaker.killjoy.valorant.ValorantAgent
import tv.blademaker.killjoy.valorant.ValorantEntity
import tv.blademaker.killjoy.valorant.ValorantMap
import tv.blademaker.killjoy.valorant.ValorantWeapon
import kotlin.jvm.Throws

object Loaders {

    private val log = LoggerFactory.getLogger(Loaders::class.java)

    fun loadAgents(): List<ValorantAgent> = loadValorantEntities(ValorantAgent::class.java, "agents")
    fun loadArsenal(): List<ValorantWeapon> = loadValorantEntities(ValorantWeapon::class.java, "arsenal")
    fun loadMaps(): List<ValorantMap> = loadValorantEntities(ValorantMap::class.java, "maps")

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
        val index = this::class.java.getResource("/$resourcePath/index.txt").readText().split("\n")
        check(index.isNotEmpty()) { "${resourcePath.capitalize()} index cannot be empty or null." }

        for (entityName in index) {
            val file = this::class.java.getResource("/$resourcePath/${entityName.toLowerCase()}.json")
                ?: throw IllegalStateException("/$resourcePath/$entityName.json is not present")

            val fileContent = file.readText()
            if (fileContent.isEmpty()) throw IllegalStateException("/$resourcePath/$entityName.json is empty")
            list.add(clazz.getConstructor(JSONObject::class.java).newInstance(JSONObject(fileContent)))
        }

        log.info("Loaded ${list.size} ${clazz.simpleName} entities!! [${list.joinToString(", ") { it.name }}]")
        return list
    }
}