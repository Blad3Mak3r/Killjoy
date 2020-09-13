/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member

/*val Member.isBotOwner: Boolean
    get() = Config.owners.any { it == this.id }*/

val Member.isSelf: Boolean
    get() = this.jda.selfUser.id == this.user.id

val Member.isDeafened: Boolean
    get() = this.voiceState!!.isDeafened

val Member.isManager: Boolean
    get() = hasPermission(Permission.MANAGE_SERVER)

val Member.isAdmin: Boolean
    get() = hasPermission(Permission.ADMINISTRATOR)