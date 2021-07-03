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

package dev.killjoy.i18n

import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.slash.api.SlashCommandContext
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.LoggerFactory
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object I18n {

    private val initialized = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(I18n::class.java)
    private val DEFAULT_LOCALE = Locale("en")
    val VALID_LOCALES = listOf(DEFAULT_LOCALE, Locale("es"))

    private val generalBundle = I18nBundle()
    private val commandsBundle = I18nBundle()

    fun init() {
        if (initialized.compareAndSet(false, true)) {
            for (loc in VALID_LOCALES.map { it.language }) {
                generalBundle[loc] = ResourceBundle.getBundle("i18n.general_$loc")
                commandsBundle[loc] = ResourceBundle.getBundle("i18n.commands_$loc")
            }

            logger.info("i18n loaded => ${generalBundle.keys} keys for messages and ${commandsBundle.keys} keys for commands.")
        } else error("I18n is already initialized.")
    }

    fun getTranslate(ctx: SlashCommandContext, key: I18nKey, vararg args: Any?): String {
        return MessageFormat.format(getImpl(ctx.guild.supportedLocale, key.pattern), *args)
    }

    fun getTranslate(guild: Guild, key: I18nKey, vararg args: Any?): String {
        return MessageFormat.format(getImpl(guild.supportedLocale, key.pattern), *args)
    }

    fun getTranslate(key: I18nKey, vararg args: Any?): String {
        return MessageFormat.format(getImpl(DEFAULT_LOCALE, key.pattern), *args)
    }

    private fun getImpl(locale: Locale, key: String): String {
        return try {
            generalBundle[locale.language]!!.getString(key)
        } catch (ex: Exception) {
            "{${key}}"
        }
    }

    fun getCommandTranslate(ctx: SlashCommandContext, key: String, vararg args: Any?): String {
        return MessageFormat.format(getCommandImpl(ctx.guild, key), *args)
    }

    fun getCommandTranslate(guild: Guild, key: String, vararg args: Any?): String {
        return MessageFormat.format(getCommandImpl(guild, key), *args)
    }

    private fun getCommandImpl(guild: Guild, key: String): String {
        return try {
            commandsBundle[guild.supportedLocale.language]!!.getString(key)
        } catch (e: Throwable) {
            "{${key}}"
        }
    }

    fun getLocale(guild: Guild): Locale {
        return VALID_LOCALES.find { it.language == guild.locale.language } ?: DEFAULT_LOCALE
    }

    fun isSupported(locale: Locale): Boolean {
        return VALID_LOCALES.any { it.language == locale.language }
    }
}