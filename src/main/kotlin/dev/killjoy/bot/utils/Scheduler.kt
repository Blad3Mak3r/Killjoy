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

package dev.killjoy.bot.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.sentry.Sentry
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Run scheduled task suppressing exceptions.
 *
 * @author Blad3Mak3r
 */
object Scheduler {

    /**
     * Run a task at fixed rate and exception suppressing.
     *
     * @author Blad3Mak3r
     *
     * @param executor and ScheduledExecutorService
     * @param initialDelay Long
     * @param delay Long
     * @param unit TimeUnit
     * @param block The code block to schedule
     */
    inline fun atFixedRateSilent(
        executor: ScheduledExecutorService,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit,
        crossinline block: () -> Unit
    ): ScheduledFuture<*> {
        return executor.scheduleAtFixedRate({
            try {
                block()
            } catch (e: Throwable) {
                Sentry.captureException(e)
            }
        }, initialDelay, delay, unit)
    }

    /**
     * Run a task with fixed delay and exception suppressing.
     *
     * @author Blad3Mak3r
     *
     * @param executor and ScheduledExecutorService
     * @param initialDelay Long
     * @param delay Long
     * @param unit TimeUnit
     * @param block The code block to schedule
     */
    inline fun withFixedDelaySilent(
        executor: ScheduledExecutorService,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit,
        crossinline block: () -> Unit
    ): ScheduledFuture<*> {
        return executor.scheduleWithFixedDelay({
            try {
                block()
            } catch (e: Throwable) {
                Sentry.captureException(e)
            }
        }, initialDelay, delay, unit)
    }

    /**
     * Creates a new ScheduledExecutorService
     */
    fun newScheduledExecutor(name: String, threads: Int = 1): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(threads, ThreadFactoryBuilder().setNameFormat(name).build())
    }
}