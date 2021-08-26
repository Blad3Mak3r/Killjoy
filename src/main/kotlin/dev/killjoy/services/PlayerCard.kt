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

@file:Suppress("DuplicatedCode")

package dev.killjoy.services

import dev.killjoy.valorant.agent.ValorantAgent
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.AttributedString
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.math.abs


data class PlayerStats(
    val name: String,
    val tag: String,
    val kills: Int,
    val deaths: Int,
    val wins: Int,
    val loses: Int,
    val mmr: Int? = null
)

object PlayerCard {

    private const val MAX_AGENT_HEIGHT = 1400

    fun generate(agent: ValorantAgent, playerStats: PlayerStats) = CompletableFuture.supplyAsync {
        val image = ImageIO.read(getBackGroundImage())
        val g = image.graphics

        drawPlayerName(g, playerStats)
        drawAgentImage(g, agent)

        addRectangle(g, image.width, image.height)

        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        out.toByteArray()
    }

    private fun getBackGroundImage(): InputStream? {
        return this::class.java.getResourceAsStream("/images/player-card.png")
    }

    private fun drawPlayerName(g: Graphics, playerStats: PlayerStats) {
        val openSans = Font.getFont("Open Sans")

        val name = AttributedString(playerStats.name.uppercase())
        name.addAttribute(TextAttribute.FONT, openSans)
        name.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
        name.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD)
        name.addAttribute(TextAttribute.SIZE, 86)

        g.drawString(name.iterator, 64, 130)

        val tag = AttributedString(playerStats.tag.uppercase())
        tag.addAttribute(TextAttribute.FONT, openSans)
        tag.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
        tag.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD)
        tag.addAttribute(TextAttribute.SIZE, 86)

        g.drawString(tag.iterator, 130, 215)
    }

    private fun drawAgentImage(g: Graphics, agent: ValorantAgent) {
        val image = this::class.java.getResourceAsStream("/images/agents/${agent.name.lowercase()}.png").use {
            ImageIO.read(it)
        }

        val bgImage = this::class.java.getResourceAsStream("/images/agents/${agent.name.lowercase()}.png").use {
            ImageIO.read(it)
        }

        val ratio = image.height.toDouble() / image.width.toDouble()

        val distance = abs(image.height - MAX_AGENT_HEIGHT)

        val width = (image.width + (distance / ratio)).toInt()
        val height = image.height + distance

        println("Ratio: $ratio")
        println("Width: ${image.width} > $width")
        println("Height: ${image.height} > $height")

        g.drawImage(colorImage(bgImage), -312, 250, width, height, null)
        g.drawImage(image, -292, 250, width, height, null)
    }

    private fun colorImage(image: BufferedImage, pixel0: Int = 0, pixel1: Int = 0, pixel2: Int = 0): BufferedImage {

        val width = image.width
        val height = image.height
        val raster = image.raster
        for (xx in 0 until width) {
            for (yy in 0 until height) {
                val pixels = raster.getPixel(xx, yy, null as IntArray?)
                pixels[0] = pixel0
                pixels[1] = pixel1
                pixels[2] = pixel2
                raster.setPixel(xx, yy, pixels)
            }
        }
        return image
    }

    private fun addRectangle(g: Graphics, width: Int, height: Int) {
        g.color = Color.decode("#ff4753")

        g.fillRect(0, 0, width, 24)
        g.fillRect(0, height-24, width, 24)

        g.fillRect(0, 0, 24, height)
        g.fillRect(width-24, 24, 24, height)
    }

}