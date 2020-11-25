/*
 * Copyright (c) 2020.
 * BladeMaker
 */
package tv.blademaker.killjoy.framework

import net.dv8tion.jda.api.Permission

enum class Category(
        val emoji: String,
        val isEnabled: Boolean,
        val isPublic: Boolean,
        val permissions: Set<Permission>,
        val botPermissions: Set<Permission>
) {
    Owner(
            "",
            true,
            false,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_EXT_EMOJI
            )
    ),
    Information(
            "\uD83D\uDD16",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_EXT_EMOJI
            )
    ),
    Game(
            "",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_EXT_EMOJI
            )
    ),
    Misc(
            "",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_EXT_EMOJI
            )
    );


}