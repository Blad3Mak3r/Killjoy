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

const val INVITE_URL = "https://discord.com/oauth2/authorize?client_id=706887214088323092&permissions=321600&scope=bot+applications.commands"
const val REPOSITORY_URL = "https://github.com/Blad3Mak3r/Killjoy"
const val WEBSITE_URL = "https://killjoy.dev"
const val BUG_REPORT_URL = "https://github.com/Blad3Mak3r/Killjoy/issues/new?template=bug_report.md"
const val VOTE_URL = "https://top.gg/bot/706887214088323092/vote"
const val SPONSOR_URL = "https://github.com/sponsors/Blad3Mak3r"

internal inline fun <reified T> getConfig(property: String, fallback: T): T =
    Credentials.getOrDefault("database.$property", fallback)