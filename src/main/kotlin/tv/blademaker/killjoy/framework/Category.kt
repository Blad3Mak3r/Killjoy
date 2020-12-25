/*******************************************************************************
 * Copyright (c) 2020. Blademaker
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