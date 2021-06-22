/*******************************************************************************
 * Copyright (c) 2021. Blademaker
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

/*******************************************************************************
 * Copyright (c) 2021. Blademaker
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

package dev.killjoy.apis.memes

import org.json.JSONObject
import kotlin.random.Random

data class Meme (
    val id: String,
    val subReddit: String,
    private val _title: String,
    val author: String,
    val image: String,
    val ups: Int,
    val downs: Int,
    val score: Int,
    val comments: Int,
    val isNsfw: Boolean,
    val createdAt: Long
    ) {

    private constructor(obj: JSONObject) : this(
        obj.getString("id"),
        obj.getString("subreddit"),
        obj.getString("title"),
        obj.getString("author"),
        obj.getString("url"),
        obj.getInt("ups"),
        obj.getInt("downs"),
        obj.getInt("score"),
        obj.getInt("num_comments"),
        obj.getBoolean("over_18"),
        obj.getLong("created_utc")
    )

    val title: String = if (_title.length > 256) _title.substring(0, 255) else _title

    val permanentLink = "https://reddit.com/$id"

    override fun toString(): String {
        return "Meme(id='$id', subReddit='$subReddit', title='$title', author='$author', image='$image', ups=$ups, downs=$downs, score=$score, comments=$comments, isNsfw=$isNsfw, createdAt=$createdAt)"
    }

    companion object {

        @Throws(IllegalArgumentException::class)
        fun buildFromJson(jsonObject: JSONObject): Meme {
            val posts = jsonObject.getJSONObject("data").getJSONArray("children")

            for (i in 0..25) {
                val random = Random.nextInt(posts.length())

                val selected = posts.getJSONObject(random).getJSONObject("data")
                if (isImage(selected.getString("url"))) {
                    return Meme(selected)
                }
            }

            throw IllegalArgumentException()
        }

        private fun isImage(str: String? = null): Boolean {
            if (str == null) return false
            return str.endsWith(".png") || str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".gif")
        }
    }

}