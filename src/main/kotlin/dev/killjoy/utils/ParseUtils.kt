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

package dev.killjoy.utils

import java.text.DateFormat
import java.time.Instant
import java.util.*

object ParseUtils {
    private fun seconds(millis: Long) = millis / 1000
    private fun minutes(seconds: Long) = seconds / 60
    private fun hours(minutes: Long) = minutes / 60
    private fun days(hours: Long) = hours / 24

    /**
     * Convert milliseconds to time format.
     *
     * @param millis The milliseconds.
     * @param showHours Should show hours if do not have hours?
     * @param showMinutes Should show minutes if do not have minutes?
     *
     * @return dd:hh:MM:ss | hh:MM:ss | MM:ss
     */
    fun millisToTime(millis: Long, showHours: Boolean = false, showMinutes: Boolean = true): String {
        var secs = seconds(millis)
        var minutes = minutes(secs)
        var hours = hours(minutes)

        secs %= 60
        minutes %= 60
        hours %= 24

        return buildString {
            if (showHours || hours > 0) append("%02d:".format(hours))
            if (showMinutes || hours > 0 || minutes > 0) append("%02d:".format(minutes))
            append("%02d".format(secs))
        }
    }

    /**
     *
     * @return dd days, hh hours, MM minutes and ss seconds.
     */
    fun millisToUptime(millis: Long): String {
        var secs = seconds(millis)
        var minutes = minutes(secs)
        var hours = hours(minutes)
        val days = days(hours)

        secs %= 60
        minutes %= 60
        hours %= 24

        return "$days days, $hours hours, $minutes minutes and $secs seconds"
    }

    fun parsedToMillis(time: String): Long? {
        val parts = time.split(":")

        if (parts.isEmpty()) return null

        val values = mutableListOf<Int>()

        return try {
            for (part in parts) {
                val value = part.toInt()
                values.add(value)
            }

            when (values.size) {
                1 -> values[0] * 1000L
                2 -> ((values[0] * 60L) + values[1]) * 1000L
                3 -> ((values[0] * 60L * 60L) + (values[1] * 60L) + values[2]) * 1000L
                else -> null
            }
        } catch (ex: Exception) {
            null
        }
    }

    // Pagination

    fun getPageIndex(page: Int) = if (page <= 1) 0 else page - 1

    fun getPageIndex(page: Int, totalPages: Int): Int {
        return if (totalPages <= 0 || page <= 1) 0
        else page.coerceAtMost(totalPages) - 1
    }

    // SNOWFLAKES

    fun parseSnowflakeToTimestamp(snowflake: Long) = (snowflake shr 22) + 1420070400000L

    fun parseSnowflakeToDateFormat(snowflake: Long): String {
        val epoch = parseSnowflakeToTimestamp(snowflake)
        val date = Date.from(Instant.ofEpochMilli(epoch))
        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(date)
        val timeFormat = DateFormat.getTimeInstance(DateFormat.LONG, Locale.US).format(date)
        return "**$dateFormat** at **$timeFormat**"
    }
}