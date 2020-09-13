/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.MessageBuilder

fun MessageBuilder.appendBold(msg: String) = this.append("**$msg**")