package dev.killjoy.extensions.okhttp

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->

    cont.invokeOnCancellation {
        cancel()
    }

    enqueue(object : Callback {

        private val wasCalled = AtomicBoolean(false)

        override fun onFailure(call: Call, e: IOException) {
            if (!wasCalled.getAndSet(true)) {
                cont.resumeWithException(e)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (!wasCalled.getAndSet(true)) {
                cont.resume(response)
            }
        }

    })
}

fun Call.submit(): CompletableFuture<Response> {
    val future = CompletableFuture<Response>()

    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            future.completeExceptionally(e)
        }

        override fun onResponse(call: Call, response: Response) {
            future.complete(response)
        }

    })

    return future
}

fun Call.queue(success: (Response) -> Unit, failure: (Throwable) -> Unit) {
    submit()
        .thenAccept(success)
        .exceptionally {
            failure(it)
            null
        }
}
