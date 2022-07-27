package dev.killjoy.interactions

import dev.kord.common.entity.*

private val EmptyOptions = emptyList<Option>()

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

fun parseCommandOptions(interaction: DiscordInteraction): List<Option> {
    val group = getSubCommandGroup(interaction)

    if (group != null) {
        val subCommand = getSubCommand(group)

        return if (subCommand != null) {
            subCommand.options.value ?: EmptyOptions
        } else {
            group.options.value ?: EmptyOptions
        }
    }

    val subCommand = getSubCommand(interaction)

    if (subCommand != null) {
        return subCommand.options.value ?: EmptyOptions
    }

    return interaction.data.options.value ?: EmptyOptions
}