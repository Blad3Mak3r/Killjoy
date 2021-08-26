import dev.killjoy.services.PlayerCard
import dev.killjoy.services.PlayerStats
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

class PlayerCardTest {

    val agents = Loaders.loadAgents()

    @Test
    fun `Generate Killjoy Profile`() {
        val agent = agents.find { it.name.equals("killjoy", true) }!!
        val stats = PlayerStats("Killjoy", "BOT", 1, 2, 3, 4, 5)
        val imageByteArray = PlayerCard.generate(agent, stats).get()
        val inputStream = ByteArrayInputStream(imageByteArray)
        val image = ImageIO.read(inputStream)
        ImageIO.write(image, "png", File("killjoy.png"))
    }

}