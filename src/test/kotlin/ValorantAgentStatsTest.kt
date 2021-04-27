import kotlinx.coroutines.runBlocking
import org.junit.Test
import tv.blademaker.killjoy.apis.riot.RiotAPI

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

class ValorantAgentStatsTest {

    @Test
    fun `Retrieve agent stats`() {
        val expected = 15
        val result = runBlocking { RiotAPI.AgentStatsAPI.getAgentStatsAsync().await() }

        //Check result is not empty
        check(result.isNotEmpty())

        //Check result size is equal to expected
        check(result.size == expected) { "Result is not equal to expected (${result.size} != $expected)" }
    }

    @Test
    fun `Get Killjoy stats`() {
        val result = runBlocking { RiotAPI.AgentStatsAPI.getAgentStatsAsync("killjoy").await() }

        checkNotNull(result)
    }
}