/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.entities.User

val User.isSelf: Boolean
    get() = this.id == this.jda.selfUser.id