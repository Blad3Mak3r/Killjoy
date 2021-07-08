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

import dev.killjoy.extensions.capital
import dev.killjoy.i18n.I18n
import jdk.jfr.Experimental
import java.util.*

@Experimental
object FuzzyScore {

    private fun score(term: CharSequence, query: CharSequence, locale: Locale = I18n.DEFAULT_LOCALE): Double {
        val termLowerCase = term.toString().lowercase(locale)
        val queryLowerCase = query.toString().lowercase(locale)

        var score = 0
        var termIndex = 0

        var previousMatchingCharacterIndex = Integer.MIN_VALUE

        for (queryChar in queryLowerCase) {

            var termCharacterMatchFound = false
            while (termIndex < termLowerCase.length && !termCharacterMatchFound) {
                val termChar = termLowerCase[termIndex]
                if (queryChar == termChar) {
                    score++
                    if (previousMatchingCharacterIndex + 1 == termIndex) {
                        score += 2
                    }
                    previousMatchingCharacterIndex = termIndex
                    termCharacterMatchFound = true
                }
                termIndex++
            }

        }

        return score.toDouble()
    }

    fun similar(term: CharSequence, list: List<CharSequence>, locale: Locale = I18n.DEFAULT_LOCALE): List<String> {
        val finalList = mutableListOf<String>()

        val requiredScore = (term.length / 1.50)

        for (item in list) {
            val score = score(term, item, locale).let { if (it > 0) it / 0.50 else it }

            if (score >= requiredScore) finalList.add(item.toString().capital())
        }

        return finalList
    }

}