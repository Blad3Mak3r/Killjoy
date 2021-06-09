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

import dev.killjoy.apis.riot.RiotAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AgentStatsTest {

    @Test
    fun `Retrieve agent stats`() {
        val expected = 15
        val result = runBlocking { RiotAPI.AgentStatsAPI.getAgentStatsAsync().await() }

        assert(result.isNotEmpty()) { "Result is empty." }
        assert(result.size == expected) { "Result is not equal to expected (${result.size} != $expected)." }
    }

    @Test
    fun `Get Killjoy stats`() {
        val result = runBlocking { RiotAPI.AgentStatsAPI.getAgentStatsAsync("killjoy").await() }

        assert(result != null) { "Result is empty." }
        assert(result!!.key == "killjoy") { "Result is not Killjoy agent." }
    }
}