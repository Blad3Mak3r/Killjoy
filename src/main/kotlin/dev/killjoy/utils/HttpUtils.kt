package dev.killjoy.utils

import dev.killjoy.extensions.okhttp.submit
import kotlinx.coroutines.future.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CompletableFuture

object HttpUtils {

    enum class MediaType(val type: okhttp3.MediaType) {
        APPLICATION_JSON("application/json".toMediaType()),
        APPLICATION_URLENCODED("application/x-www-form-urlencoded".toMediaType()),
        TEXT_HTML("text/html".toMediaType()),
        TEXT_PLAIN("text/plain".toMediaType());

        val typeString = type.toString()
    }

    private val client = OkHttpClient.Builder().build()

    suspend fun getJsonObject(url: String) = getJsonObject { url(url) }

    suspend fun getJsonObject(request: Request.Builder.() -> Unit): JSONObject {
        return await(JSONObject::class.java, request).content
            ?: throw IllegalStateException("Received empty body on request.")
    }

    fun empty(request: Request.Builder.() -> Unit) {
        val req = Request.Builder().apply(request).build()
        client.newCall(req).enqueue(EmptyEnqueue())
    }

    suspend fun await(request: Request.Builder.() -> Unit): Response {
        return send(request).await()
    }

    fun send(request: Request.Builder.() -> Unit): CompletableFuture<Response> {
        val req = Request.Builder().apply(request).build()
        return client.newCall(req).submit()
    }

    suspend fun <T> await(clazz: Class<T>, request: Request.Builder.() -> Unit): HttpResponse<T> {
        return send(clazz, request).await()
    }

    suspend fun <T> await(clazz: Class<T>, url: String) = await(clazz) { url(url) }

    fun <T> send(clazz: Class<T>, request: Request.Builder.() -> Unit): CompletableFuture<HttpResponse<T>> {
        val future = CompletableFuture<HttpResponse<T>>()

        val req = Request.Builder().apply(request).build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val body = res.body
                    when {
                        !res.isSuccessful -> future.complete(HttpResponse(res.isSuccessful, res.code, null))
                        body == null -> future.complete(HttpResponse(res.isSuccessful, res.code, null))
                        else -> {
                            future.complete(HttpResponse(res.isSuccessful, res.code, newInstance(clazz, body)))
                        }
                    }
                }
            }

        })

        return future
    }

    private fun <T> newInstance(clazz: Class<T>, body: ResponseBody): T {
        val content = body.string()
        println(content)
        return clazz.getDeclaredConstructor(String::class.java).newInstance(content)
    }

    private class EmptyEnqueue : Callback {
        override fun onFailure(call: Call, e: IOException) {
        }

        override fun onResponse(call: Call, response: Response) {
            response.close()
        }
    }

    data class HttpResponse<T>(
        val isSuccessful: Boolean,
        val code: Int,
        val content: T?
    )
}