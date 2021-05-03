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

package net.hugebot.extensions.jda

import io.sentry.Sentry
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Run a task at fixed rate and exception suppressing.
 *
 * @author Blad3Mak3r
 *
 * @param initialDelay Long
 * @param delay Long
 * @param unit TimeUnit
 * @param block The code block to schedule
 */
inline fun ScheduledExecutorService.scheduleAtFixedRateCatching(
    initialDelay: Long,
    delay: Long,
    unit: TimeUnit,
    crossinline block: () -> Unit
): ScheduledFuture<*> {
    return this.scheduleAtFixedRate({
        try {
            block()
        } catch (e: Throwable) {
            Sentry.captureException(e)
        }
    }, initialDelay, delay, unit)
}