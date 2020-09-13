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
    Basic(
            "",
            "\uD83D\uDD16",
            true,
            true,
            hashSetOf(),
            hashSetOf(
                    Permission.MESSAGE_EMBED_LINKS
            )
    );


}