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

import java.util.*
import kotlin.math.floor

object Algorithms {

    private const val REQUIRE_MINIMUM_DISTANCE = 0.75

    fun dictionarySimilar(term: String, dictionary: List<String>): List<String> {
        val resultList = mutableListOf<String>()

        for (item in dictionary) {
            val distance = JaroWinkler.distance(term, item)
            if (distance >= REQUIRE_MINIMUM_DISTANCE) {
                resultList.add(item)
            } else {
                val anagram = Anagram.of(term, item)
                if (anagram) resultList.add(item)
            }
        }

        return resultList
    }

    object Anagram {
        fun of(s1: String, s2: String): Boolean {
            val a1 = s1.lowercase().replace("\\s+".toRegex(), "").split("").toTypedArray()
            val a2 = s2.lowercase().replace("\\s+".toRegex(), "").split("").toTypedArray()

            Arrays.sort(a1)
            Arrays.sort(a2)

            return a1.contentEquals(a2)
        }
    }

    object Jaro {
        fun distance(str1: String, str2: String): Double {
            val s1 = str1.lowercase()
            val s2 = str2.lowercase()

            // If the strings are equal
            if (s1 === s2) return 1.0

            // Length of two strings
            val len1 = s1.length
            val len2 = s2.length
            if (len1 == 0 || len2 == 0) return 0.0

            // Maximum distance upto which matching
            // is allowed
            val maxDist = floor((len1.coerceAtLeast(len2) / 2).toDouble()).toInt() - 1

            // Count of matches
            var match = 0

            // Hash for matches
            val hashS1 = IntArray(s1.length)
            val hashS2 = IntArray(s2.length)

            // Traverse through the first string
            for (i in 0 until len1) {

                // Check if there is any matches
                for (j in 0.coerceAtLeast(i - maxDist) until len2.coerceAtMost(i + maxDist + 1))  // If there is a match
                    if (s1[i] == s2[j] &&
                        hashS2[j] == 0
                    ) {
                        hashS1[i] = 1
                        hashS2[j] = 1
                        match++
                        break
                    }
            }

            // If there is no match
            if (match == 0) return 0.0

            // Number of transpositions
            var t = 0.0
            var point = 0

            // Count number of occurrences
            // where two characters match but
            // there is a third matched character
            // in between the indices
            for (i in 0 until len1) if (hashS1[i] == 1) {

                // Find the next matched character
                // in second string
                while (hashS2[point] == 0) point++
                if (s1[i] != s2[point++]) t++
            }
            t /= 2.0

            // Return the Jaro Similarity
            return ((match.toDouble() / len1.toDouble() + match.toDouble() / len2.toDouble() + (match.toDouble() - t) / match.toDouble())
                    / 3.0)
        }
    }

    object JaroWinkler {

        fun distance(s1: String, s2: String): Double {
            var jaroDist: Double = Jaro.distance(s1, s2)

            // If the jaro Similarity is above a threshold
            if (jaroDist > 0.7) {

                // Find the length of common prefix
                var prefix = 0
                for (i in 0 until s1.length.coerceAtMost(s2.length)) {

                    // If the characters match
                    if (s1[i] == s2[i]) prefix++ else break
                }

                // Maximum of 4 characters are allowed in prefix
                prefix = 4.coerceAtMost(prefix)

                // Calculate jaro winkler Similarity
                jaroDist += 0.1 * prefix * (1 - jaroDist)
            }
            return jaroDist
        }

    }

}