import org.junit.Test
import dev.killjoy.utils.Loaders

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

class LoadersTest {

    @Test
    fun `Load agents`() {
        val expected = 18
        val result = Loaders.loadAgents()
        assert(result.size == expected) { "Agents size is not equal to expected" }
    }

    @Test
    fun `Load maps`() {
        val expected = 7
        val result = Loaders.loadMaps()
        assert(result.size == expected) { "Maps size is not equal to expected" }
    }

    @Test
    fun `Load arsenal`() {
        val expected = 18
        val result = Loaders.loadArsenal()
        assert(result.size == expected) { "Arsenal size is not equal to expected" }
    }

}