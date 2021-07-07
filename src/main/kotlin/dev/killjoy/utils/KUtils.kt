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

import tv.blademaker.slash.api.SlashCommandContext
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button

fun paginationButtons(ctx: SlashCommandContext, disabled: Boolean = false): ActionRow {
    val interactionID = ctx.hook.interaction.id

    return ActionRow.of(
        Button.secondary("${interactionID}:preview", Emoji.fromUnicode("⏮️")).withDisabled(disabled),
        Button.secondary("${interactionID}:next", Emoji.fromUnicode("⏭️")).withDisabled(disabled),
        Button.danger("${interactionID}:cancel", Emoji.fromUnicode("\uD83D\uDED1")).withDisabled(disabled)
    )
}

fun userInteractionFilter(event: ButtonClickEvent, author: User, interactionID: String): Boolean {
    return event.componentId.split(":")[0] == interactionID &&
            event.user.idLong == author.idLong
}