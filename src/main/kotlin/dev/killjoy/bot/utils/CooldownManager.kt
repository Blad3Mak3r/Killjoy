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

import dev.killjoy.bot.framework.CommandContext
import dev.killjoy.bot.framework.abs.Command
import dev.killjoy.bot.framework.annotations.Cooldown
import net.hugebot.extensions.jda.scheduleAtFixedRateCatching
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class CooldownManager(
    checkInterval: Long,
    unit: TimeUnit,
    executor: ScheduledExecutorService = Scheduler.newScheduledExecutor("x-cooldown", 1)
) {

    private val store = ConcurrentHashMap<String, Bucket>()

    private val task: ScheduledFuture<*> = executor.scheduleAtFixedRateCatching(checkInterval, checkInterval, unit) {
        store.forEach {
            val bucket = it.value
            if (bucket.timestamp < System.currentTimeMillis()) store.remove(it.key)
        }
    }

    fun shutdown() {
        task.cancel(false)
        store.clear()
    }

    @Synchronized
    fun check(ctx: CommandContext, cmd: Command): BucketResult? {
        val cd = cmd.props.cooldown
        val key = buildBucketName(ctx, cmd)

        val bucket = store[key]

        if (bucket == null || bucket.rest() <= 0) {
            if (cd.value > 0) {
                store[key] = Bucket.new(cd.timeUnit.toMillis(cd.value))
                log.info("Registering CD:BUCKET:[$key] for ${cd.value} ${cd.timeUnit.name}.")
            }
            return null
        }

        return BucketResult(bucket.getAndSetAnnounce(), bucket.rest())
    }

    @Synchronized
    fun cancelBucket(ctx: CommandContext, cmd: Command) {
        val key = buildBucketName(ctx, cmd)
        if (store.containsKey(key)) {
            log.info("Cancelling CD:BUCKET:[$key]")
            store.remove(key)
        }
    }

    private fun buildBucketName(ctx: CommandContext, cmd: Command): String {
        return if (cmd.props.cooldown.type == Cooldown.Type.User) {
            "${ctx.guild.id}::${ctx.author.id}::${cmd.props.name}"
        } else {
            "${ctx.guild.id}::${cmd.props.name}"
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CooldownManager::class.java)
    }

    class Bucket private constructor(val timestamp: Long) {
        private val announced = AtomicBoolean(false)

        fun rest(): Long {
            val current = System.currentTimeMillis()
            return if (timestamp < current) 0L
            else (timestamp - current)
        }

        fun getAndSetAnnounce() = announced.getAndSet(true)

        companion object {
            fun new(base: Long): Bucket {
                return Bucket(System.currentTimeMillis() + base)
            }
        }
    }

    data class BucketResult(
        val announced: Boolean,
        val rest: Long
    ) {
        val restInSeconds = TimeUnit.MILLISECONDS.toSeconds(rest)
    }
}