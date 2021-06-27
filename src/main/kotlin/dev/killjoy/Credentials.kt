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

package dev.killjoy

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

object Credentials {

    private fun getConfigFile(): File {
        val file = File("credentials.conf")

        if (!file.exists()) throw IllegalStateException("Can not found credentials.conf file.")
        return file
    }

    private val conf = ConfigFactory.parseFile(getConfigFile())

    internal inline operator fun <reified T> get(path: String): T {
        val ref = conf.getAnyRef(path)
        if (ref is T) return ref
        else throw IllegalStateException("Reference is not ${T::class}")
    }

    internal fun getNullableConfigList(path: String): List<Config>? {
        return try {
            conf.getConfigList(path)
        } catch (e: Exception) {
            null
        }
    }

    internal inline fun <reified T> getOrNull(path: String): T? {
        return try {
            val ref = conf.getAnyRef(path)
            if (ref is T) ref
            else null
        } catch (e: Throwable) {
            null
        }
    }

    internal inline fun <reified T> getOrDefault(path: String, fallback: T): T {
        return try {
            val ref = conf.getAnyRef(path)
            if (ref is T) ref
            else fallback
        } catch (e: Throwable) {
            return fallback
        }
    }

    //KUtils
    internal inline fun <reified T> Config.get(path: String): T {
        val ref = this.getAnyRef(path)
        if (ref is T) return ref
        else throw IllegalStateException("Reference is not ${T::class}")
    }

    @Suppress("unused")
    internal inline fun <reified T> Config.getOrNull(path: String): T? {
        return try {
            val ref = this.getAnyRef(path)
            if (ref is T) ref
            else null
        } catch (e: Throwable) {
            null
        }
    }

    internal inline fun <reified T> Config.getOrDefault(path: String, fallback: T): T {
        return try {
            val ref = this.getAnyRef(path)
            if (ref is T) ref
            else fallback
        } catch (e: Throwable) {
            return fallback
        }
    }

    // Base config
    val token: String = get("token")
    val synchronize: Boolean = getOrDefault("database.synchronize", false)
}