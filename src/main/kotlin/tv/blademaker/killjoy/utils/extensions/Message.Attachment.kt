/*
 * Copyright (c) 2020.
 * BladeMaker
 */

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.entities.Message

private val AUDIO_EXTENSIONS = setOf("flac", "mkv", "mp4", "mp3", "ogg", "wav")

val Message.Attachment.isAudio: Boolean
    get() {
        val extension = this.fileExtension
        return extension != null && AUDIO_EXTENSIONS.contains(extension.toLowerCase())
    }