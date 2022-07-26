package dev.killjoy.interactions

import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.Option

fun parseCommandPath(interaction: DiscordInteraction): String {
    return buildString {
        append(interaction.data.name)
        getSubCommandGroup(interaction)?.let { append("/${it.name}") }
        getSubCommand(interaction)?.let { append("/${it.name}") }
    }
}

private fun getSubCommandGroup(interaction: DiscordInteraction): Option? {
    return interaction.data.options.value?.find { it.type == ApplicationCommandOptionType.SubCommandGroup }
}

private fun getSubCommand(interaction: DiscordInteraction): Option? {
    return interaction.data.options.value?.find { it.type == ApplicationCommandOptionType.SubCommand }
}