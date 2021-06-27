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

package dev.killjoy.commands.info

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.killjoy.Launcher
import dev.killjoy.extensions.jda.await
import dev.killjoy.slash.api.AbstractSlashCommand
import dev.killjoy.slash.api.SlashCommandContext
import dev.killjoy.slash.api.annotations.Permissions
import dev.killjoy.slash.api.annotations.SlashSubCommand
import dev.killjoy.utils.Emojis
import dev.killjoy.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import java.lang.Exception
import java.lang.IllegalStateException

@Permissions([Permission.MANAGE_SERVER])
class SetupSlashCommand : AbstractSlashCommand("setup") {

    @SlashSubCommand(group = "news", name = "enable")
    @Permissions(bot = [Permission.MANAGE_WEBHOOKS])
    suspend fun newsEnable(ctx: SlashCommandContext) {
        val alreadyExists = Launcher.database.newsWebhook.exists(ctx.guild)

        if (alreadyExists) return ctx.reply(Emojis.NoEntry, "Automatic news is already **enabled** for this Discord server.")
            .setEphemeral(true)
            .queue()

        val channel = ctx.getOption("channel")!!.asMessageChannel?.let { it as TextChannel }
            ?: return ctx.reply(Emojis.NoEntry, "Selected channel is not a valid message channel.")
                .setEphemeral(true)
                .queue()

        ctx.acknowledge().queue()

        val hook = channel.createWebhook("Killjoy News")
            .setAvatar(Utils.getAvatarIcon(ctx))
            .await()

        try {
            Launcher.database.newsWebhook.create(ctx.guild, hook)

            ctx.send(Emojis.Success, "Automatic news has been enabled on the ${channel.asMention} channel!").queue()

            WebhookClient.withUrl(hook.url)

            val message = WebhookMessageBuilder().apply {
                this.setAvatarUrl(ctx.jda.selfUser.effectiveAvatarUrl)
                this.setUsername("Killjoy News")
                this.append("Automatic news was enabled on this channel!")
            }.build()
            WebhookClientBuilder.fromJDA(hook).build().run { send(message) }
        } catch (e: Exception) {
            channel.deleteWebhookById(hook.id).queue()
            throw e
        }
    }

    @SlashSubCommand(group = "news", name = "disable")
    @Permissions(bot = [Permission.MANAGE_WEBHOOKS])
    suspend fun newsDisable(ctx: SlashCommandContext) {
        val alreadyExists = Launcher.database.newsWebhook.exists(ctx.guild)

        if (!alreadyExists) return ctx.reply(Emojis.NoEntry, "Automatic news is already **disabled** for this Discord server.")
            .setEphemeral(true)
            .queue()

        ctx.acknowledge().queue()

        val hook = Launcher.database.newsWebhook.find(ctx.guild)
            ?: throw IllegalStateException("Hook is empty on '/setup news disable' after check")

        val channel = ctx.guild.getTextChannelById(hook.channelId)

        channel?.deleteWebhookById(hook.hookID.toString())?.queue({ }, { })

        Launcher.database.newsWebhook.remove(hook)

        ctx.send(Emojis.Success, "Automatic news has been disabled for this server.").queue()
    }
}