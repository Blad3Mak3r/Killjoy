/*
 * Copyright (c) 2020. Blad3Mak3r
 */

package net.hugebot.extensions.jda

import net.dv8tion.jda.api.requests.RestAction
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> RestAction<T>.await() = suspendCoroutine<T> { cont ->
    queue(
            cont::resume,
            cont::resumeWithException
    )
}