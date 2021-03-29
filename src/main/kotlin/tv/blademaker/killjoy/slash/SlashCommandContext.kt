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

package tv.blademaker.killjoy.slash

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.commands.CommandHook
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.utils.Emojis

class SlashCommandContext(
    val event: SlashCommandEvent
) {

    val hook: CommandHook
        get() = event.hook

    val options: List<SlashCommandEvent.OptionData>
        get() = event.options

    val selfMember: Member
        get() = event.guild!!.selfMember

    val channel: TextChannel
        get() = event.channel as TextChannel

    fun acknowledge(ephemeral: Boolean = false) = event.acknowledge(ephemeral)

    fun getOption(name: String) = event.getOption(name)

    fun send(content: String) = hook.sendMessage(content)

    fun send(emoji: Emojis, message: String) = hook.sendMessage("${emoji.getCode(this)} $message")

    fun send(embed: MessageEmbed) = hook.sendMessage(embed)

    fun send(embedBuilder: EmbedBuilder) = hook.sendMessage(embedBuilder.build())

    fun sendEmbed(builder: EmbedBuilder.() -> Unit): InteractionWebhookAction {
        val embed = EmbedBuilder()
            .setColor(ColorExtra.VAL_RED)
            .apply(builder).build()

        return hook.sendMessage(embed)
    }
}