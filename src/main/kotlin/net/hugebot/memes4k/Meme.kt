package net.hugebot.memes4k

import org.json.JSONObject
import kotlin.random.Random

data class Meme (
    val id: String,
    val subReddit: String,
    val title: String,
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