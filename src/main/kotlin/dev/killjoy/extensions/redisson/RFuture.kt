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

package dev.killjoy.extensions.redisson

import kotlinx.coroutines.suspendCancellableCoroutine
import org.redisson.api.RFuture
import kotlin.coroutines.resumeWithException

suspend fun <V> RFuture<V>.awaitSuspend() = suspendCancellableCoroutine<V> { cont ->
    cont.invokeOnCancellation {
        this.cancel(true)
    }

    this.thenAccept {
        cont.resumeWith(Result.success(it))
    }.exceptionally {
        cont.resumeWithException(it)
        null
    }
}