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

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.entities.Message

private val AUDIO_EXTENSIONS = setOf("flac", "mkv", "mp4", "mp3", "ogg", "wav")

val Message.Attachment.isAudio: Boolean
    get() {
        val extension = this.fileExtension
        return extension != null && AUDIO_EXTENSIONS.contains(extension.toLowerCase())
    }