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

@file:Suppress("unused")

package dev.killjoy.commands.info

import dev.killjoy.INVITE_URL
import dev.killjoy.VOTE_URL
import dev.killjoy.WEBSITE_URL
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button

class HelpSlashCommand : AbstractSlashCommand("help") {

    override suspend fun handle(ctx: SlashCommandContext) {
        ctx.replyEmbed {
            setTitle("Did you need help?")
            appendDescription("Killjoy apart from being your favorite character, " +
                    "is a bot focused on the world of **Valorant**, quite complete and easy to understand and learn to use.")
            appendDescription("With the following links, you can find all the information you are looking for to get the most out of **Killjoy**.")
            addField("Vote for Killjoy", "If you like **Killjoy** and its content, don't forget to show your " +
                    "support, by **voting for Killjoy on top.gg**, you can vote every 12 hours and for us it is a great " +
                    "show of support from you.", false)
        }.addActionRows(ACTION_ROWS).queue()
    }

    companion object {
        val ACTION_ROWS = ActionRow.of(
            Button.link(INVITE_URL, "Invite"),
            Button.link(WEBSITE_URL, "Website"),
            Button.link("$WEBSITE_URL/commands", "Commands"),
            Button.link(VOTE_URL, "Vote")
        )
    }
}