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

package dev.killjoy.extensions

import net.dv8tion.jda.api.entities.Guild
import org.slf4j.Logger

fun Logger.info(guild: Guild, content: String) = this.info("[\u001b[32m${guild.name}(${guild.id})\u001b[0m] $content")
fun Logger.warn(guild: Guild, content: String) = this.warn("[\u001b[33m${guild.name}(${guild.id})\u001b[0m] $content")
fun Logger.error(guild: Guild, content: String, ex: Throwable? = null) = this.error("\u001b[31m[${guild.name}(${guild.id}) $content\u001b[0m", ex)