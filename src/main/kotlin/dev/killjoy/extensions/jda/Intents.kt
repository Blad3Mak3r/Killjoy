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

package dev.killjoy.extensions.jda

import net.dv8tion.jda.api.requests.GatewayIntent

object Intents {
    val disabledIntents = listOf(
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.GUILD_EMOJIS,
        GatewayIntent.GUILD_WEBHOOKS,
        GatewayIntent.GUILD_INVITES,
        GatewayIntent.GUILD_PRESENCES,
        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
        GatewayIntent.DIRECT_MESSAGE_TYPING,
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
        GatewayIntent.DIRECT_MESSAGE_TYPING,
        GatewayIntent.GUILD_VOICE_STATES
    )

    val allIntentsInt = GatewayIntent.ALL_INTENTS
    val allIntents = GatewayIntent.getIntents(allIntentsInt)
    val disabledIntentsInt = GatewayIntent.getRaw(disabledIntents)
    val enabledIntentsInt = allIntentsInt and disabledIntentsInt.inv()
    val enabledIntents = GatewayIntent.getIntents(enabledIntentsInt)
}