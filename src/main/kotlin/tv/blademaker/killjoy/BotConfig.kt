package tv.blademaker.killjoy

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import java.lang.IllegalStateException
import java.util.*

object BotConfig {
    private val conf = ConfigFactory.parseFile(File("killjoy.conf"))
    private val encoder = Base64.getEncoder()

    internal inline operator fun <reified T> get(path: String): T {
        val ref = conf.getAnyRef(path)
        if (ref is T) return ref
        else throw IllegalStateException("Reference is not ${T::class}")
    }

    internal inline fun <reified T> getOrNull(path: String): T? {
        return try {
            val ref = conf.getAnyRef(path)
            if (ref is T) ref
            else null
        } catch (e: Throwable) {
            null
        }
    }

    internal inline fun <reified T> getOrDefault(path: String, fallback: T): T {
        return try {
            val ref = conf.getAnyRef(path)
            if (ref is T) ref
            else fallback
        } catch (e: Throwable) {
            return fallback
        }
    }

    // Base config
    val token: String = get("discord.token")
}