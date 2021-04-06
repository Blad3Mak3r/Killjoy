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

package tv.blademaker.killjoy

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import java.lang.IllegalStateException
import java.util.*

object BotConfig {
    private val conf = ConfigFactory.parseFile(File("killjoy.conf"))
    private val encoder = Base64.getEncoder()

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

    // Base config
    val token: String = get("discord.token")

    //KUtils
    internal inline fun <reified T> Config.get(path: String): T {
        val ref = this.getAnyRef(path)
        if (ref is T) return ref
        else throw IllegalStateException("Reference is not ${T::class}")
    }

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
}