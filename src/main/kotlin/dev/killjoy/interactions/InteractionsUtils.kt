package dev.killjoy.interactions

import dev.kord.common.entity.*

fun parseCommandPath(interaction: DiscordInteraction): String {
    return buildString {
        append(interaction.data.name.value!!)
        getSubCommandGroup(interaction)?.let { group ->
            append("/${group.name}")
            getSubCommand(group)?.let {
                append("/${it.name}")
            }
        }
        getSubCommand(interaction)?.let { append("/${it.name}") }
    }
}

private fun getSubCommandGroup(interaction: DiscordInteraction): CommandGroup? {
    return interaction.data.options.value?.find { it.type == ApplicationCommandOptionType.SubCommandGroup } as? CommandGroup
}

private fun getSubCommand(interaction: DiscordInteraction): SubCommand? {
    return interaction.data.options.value?.find { it.type == ApplicationCommandOptionType.SubCommand } as? SubCommand
}

private fun getSubCommand(group: CommandGroup): SubCommand? {
    return group.options.value?.find { it.type == ApplicationCommandOptionType.SubCommand }
}