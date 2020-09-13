/*
 * Copyright (c) 2020.
 * BladeMaker
 */
package tv.blademaker.killjoy.framework

import net.dv8tion.jda.api.Permission

enum class Category(
        val info: String,
        val emoji: String,
        val isEnabled: Boolean,
        val isPublic: Boolean,
        val permissions: Set<Permission>,
        val botPermissions: Set<Permission>
) {
    Owner(
            "ADM",
            "",
            true,
            false,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Animals(
            "",
            "\uD83D\uDC31",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Basic(
            "This module provides basic commands for your server.",
            "\uD83D\uDD16",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Configuration(
            "This module provides configuration commands for your server.",
            "⚙️",
            true,
            true,
            hashSetOf(
                    Permission.ADMINISTRATOR
            ),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Information(
            "",
            "ℹ️",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Misc(
            "",
            "\uD83C\uDF89",
            true,
            true,
            hashSetOf(),
            hashSetOf()
    ),
    Moderation(
            "This module provides moderation commands for your server.",
            "\uD83D\uDD10",
            true,
            true,
            hashSetOf(
            ),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Music(
            "This module allows you to play your favorite music on Discord thanks to Huge",
            "\uD83C\uDFB6",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.VOICE_CONNECT,
                    Permission.VOICE_SPEAK,
                    Permission.VOICE_USE_VAD,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ADD_REACTION
            )
    ),
    NSFW(
            "NO SUITABLE FOR WORK",
            "\uD83D\uDD1E",
            true,
            false,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Utility(
            "This module provides your Guild with extra functionality.",
            "\uD83D\uDEE0️",
            true,
            true,
            hashSetOf(
                    Permission.MANAGE_CHANNEL
            ),
            hashSetOf(
                    Permission.MANAGE_CHANNEL,
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Memes(
            "",
            "<:pepohappy:693149980042592307>",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    Twitch(
            "",
            "<:twitch:694887837090185267>",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    );


}