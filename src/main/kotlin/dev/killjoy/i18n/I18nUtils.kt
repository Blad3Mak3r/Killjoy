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

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import tv.blademaker.slash.api.SlashCommandContext
import java.util.*

typealias I18nBundle = HashMap<String, ResourceBundle>

// Guild
fun Guild.i18n(key: I18nKey, vararg args: Any?) = I18n.getTranslate(this, key, *args)
fun Guild.i18nCommand(key: String, vararg args: Any?) = I18n.getCommandTranslate(this, key, *args)

// SlashCommandContext
fun SlashCommandContext.i18n(key: I18nKey, vararg args: Any?) = I18n.getTranslate(this, key, *args)
fun SlashCommandContext.i18nCommand(key: String, vararg args: Any?) = I18n.getCommandTranslate(this, key, *args)
fun SlashCommandContext.replyI18n(key: I18nKey, vararg args: Any?) = this.reply(I18n.getTranslate(this, key, *args))
fun SlashCommandContext.sendI18n(key: String, vararg args: Any?) = this.send(I18n.getCommandTranslate(this, key, *args))

// SlashCommandEvent
fun SlashCommandEvent.replyI18n(key: I18nKey, vararg args: Any?) = this.reply(I18n.getTranslate(key, *args))
fun SlashCommandEvent.sendI18n(key: I18nKey, vararg args: Any?) = this.hook.sendMessage(I18n.getTranslate(key, *args))

class I18nException(override val message: String) : RuntimeException()