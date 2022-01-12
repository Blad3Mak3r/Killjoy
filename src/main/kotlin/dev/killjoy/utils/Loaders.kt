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

package dev.killjoy.utils

import dev.killjoy.valorant.ValorantEntity
import dev.killjoy.valorant.agent.ValorantAgent
import dev.killjoy.valorant.arsenal.ValorantWeapon
import dev.killjoy.valorant.map.ValorantMap
import org.json.JSONObject
import org.reflections.Reflections
import org.reflections.ReflectionsException
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory


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
    private fun <T : ValorantEntity> loadValorantEntities(clazz: Class<T>, resourcePath: String, sort: Boolean = true): List<T> {
        val entities = mutableListOf<T>()

        val resources = Reflections(resourcePath, Scanners.Resources)
            .getResources(".*\\.json".toPattern())
            .map { "/$it" }

        for (resource in resources) {
            try {
                val file = this::class.java.getResource(resource) ?: error("$resource is not present")

                val content = file.readText()
                if (content.isEmpty()) error("$resource is empty")

                val entity = clazz.getConstructor(JSONObject::class.java).newInstance(JSONObject(content))

                entities.add(entity)
            } catch (e: Throwable) {
                log.error("Exception building Entity $resource")
                throw e
            }
        }

        log.info("Loaded ${entities.size} ${clazz.simpleName} entities!! [${entities.joinToString(", ") { it.name }}]")

        return if (sort) entities.asIterable().sortedBy { it.name }
        else entities
    }
}