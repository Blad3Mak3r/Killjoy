
import dev.killjoy.services.PlayerCard
import dev.killjoy.utils.Loaders
import dev.killjoy.valorant.agent.ValorantAgent
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

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

val agents = Loaders.loadAgents()

class PlayerCardTest {

    @Test
    fun `Astra Player Card`() = generate(agent("astra"))

    @Test
    fun `Breach Player Card`() = generate(agent("breach"))

    @Test
    fun `Brimstone Player Card`() = generate(agent("brimstone"))

    @Test
    fun `Cypher Player Card`() = generate(agent("cypher"))

    @Test
    fun `Jett Player Card`() = generate(agent("jett"))

    @Test
    fun `KAYO Player Card`() = generate(agent("kay/o"))

    @Test
    fun `Killjoy Player Card`() = generate(agent("killjoy"))

    @Test
    fun `Omen Player Card`() = generate(agent("omen"))

    @Test
    fun `Phoenix Player Card`() = generate(agent("phoenix"))

    @Test
    fun `Raze Player Card`() = generate(agent("raze"))

    @Test
    fun `Reyna Player Card`() = generate(agent("reyna"))

    @Test
    fun `Sage Player Card`() = generate(agent("sage"))

    @Test
    fun `Skye Player Card`() = generate(agent("skye"))

    @Test
    fun `Sova Player Card`() = generate(agent("sova"))

    @Test
    fun `Viper Player Card`() = generate(agent("viper"))

    @Test
    fun `Yoru Player Card`() = generate(agent("yoru"))

    private fun agent(name: String) = agents.find { it.name.equals(name, true) }!!

    private fun generate(agent: ValorantAgent) {
        val imageByteArray = PlayerCard.generate("BladeMaker", "TTV", agent, null).get()
        val inputStream = ByteArrayInputStream(imageByteArray)
        val image = ImageIO.read(inputStream)
        ImageIO.write(image, "png", File("player-card-testing/${agent.name.lowercase().replace("/", "")}.png"))
    }

}