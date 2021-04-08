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
import org.reflections.Reflections
import org.reflections.ReflectionsException
import org.reflections.scanners.ResourcesScanner
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
     * Load a list of [ValorantEntity] objects.
     *
     * @param clazz a class extending the interface [ValorantEntity].
     * @param resourcePath the path to the resource (maps, agents, arsenal).
     *
     * @throws ReflectionsException When the provided resourcePath don't exists.
     * @throws IllegalStateException When some of the resources from the resourcePath don't exists or is empty.
     *
     * @return a list of the given valorant entity [ValorantEntity].
     */
    @Throws(IllegalStateException::class, ReflectionsException::class)
    private fun <T : ValorantEntity> loadValorantEntities(clazz: Class<T>, resourcePath: String): List<T> {
        val entities = mutableListOf<T>()

        val resources = Reflections(resourcePath, ResourcesScanner())
            .getResources(".*\\.json".toPattern())
            .map { "/$it" }

        for (resource in resources) {
            val file = this::class.java.getResource(resource) ?: error("$resource is not present")

            val content = file.readText()
            if (content.isEmpty()) error("$resource is empty")

            entities.add(clazz.getConstructor(JSONObject::class.java).newInstance(JSONObject(content)))
        }

        log.info("Loaded ${entities.size} ${clazz.simpleName} entities!! [${entities.joinToString(", ") { it.name }}]")
        return entities
    }
}