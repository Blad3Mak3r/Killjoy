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

import dev.killjoy.database.models.AccountWithStats
import dev.killjoy.database.models.PlayerStats
import dev.killjoy.valorant.agent.ValorantAgent
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.AttributedString
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.math.abs

object PlayerCard {

    private val logger = LoggerFactory.getLogger(PlayerCard::class.java)
    private const val MAX_AGENT_HEIGHT = 1390

    private val OPEN_SANS = Font.getFont("Open Sans")
    private val OPEN_SANS_STATS = Font("Open Sans", Font.BOLD, 69)

    fun generate(aws: AccountWithStats, agent: ValorantAgent) = generate(aws.account.username, aws.account.gameTag, agent, aws.stats)

    fun generate(
        username: String,
        gameTag: String,
        agent: ValorantAgent,
        playerStats: PlayerStats? = null
    ): CompletableFuture<ByteArray> = CompletableFuture.supplyAsync {

        val image = ImageIO.read(getBackGroundImage())
        val g = image.createGraphics()

        drawPlayerName(g, username, gameTag)
        drawAgentImage(g, agent)

        drawStats(g, image.width, playerStats)

        drawBorder(g, image.width, image.height)

        ByteArrayOutputStream().use {
            ImageIO.write(image, "png", it)
            it
        }.toByteArray()
    }

    private fun getBackGroundImage(): InputStream? {
        return this::class.java.getResourceAsStream("/images/player-card.png")
    }

    private fun drawPlayerName(g: Graphics2D, username: String, gameTag: String) {

        val name = AttributedString(username.uppercase())
        name.addAttribute(TextAttribute.FONT, OPEN_SANS)
        name.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
        name.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD)
        name.addAttribute(TextAttribute.SIZE, 86)

        g.drawString(name.iterator, 64, 130)

        val tag = AttributedString(gameTag.uppercase())
        tag.addAttribute(TextAttribute.FONT, OPEN_SANS)
        tag.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
        tag.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD)
        tag.addAttribute(TextAttribute.SIZE, 86)

        g.drawString(tag.iterator, 130, 215)
    }

    private fun drawAgentImage(g: Graphics2D, agent: ValorantAgent) {
        val image = this::class.java.getResourceAsStream("/images/agents/${agent.name.lowercase().replace("/", "-")}.png").use {
            if (it == null) error("Cannot found agent image for ${agent.name}")
            ImageIO.read(it)
        }

        val ratio = image.height.toDouble() / image.width.toDouble()

        val distance = abs(image.height - MAX_AGENT_HEIGHT)

        val width = (image.width + (distance / ratio)).toInt()
        val height = image.height + distance
        g.drawImage(image, -292, 250, width, height, null)
    }

    private fun drawStats(g: Graphics2D, width: Int, stats: PlayerStats? = null) {
        val winsText = stats?.wins?.toString() ?: "NaN"
        val wins = AttributedString(winsText)
        wins.addAttribute(TextAttribute.FONT, OPEN_SANS_STATS)
        wins.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
        wins.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD)

        val losesText = stats?.loses?.toString() ?: "NaN"
        val loses = AttributedString(losesText)
        loses.addAttribute(TextAttribute.FONT, OPEN_SANS_STATS)
        loses.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
        loses.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD)

        logger.info(winsText)
        logger.info(losesText)

        val fontMetrics = g.getFontMetrics(OPEN_SANS_STATS)

        val winsWidth = fontMetrics.stringWidth(winsText)
        val losesWidth = fontMetrics.stringWidth(losesText)

        g.drawString(wins.iterator, (width-winsWidth)-60, 567)
        g.drawString(loses.iterator, (width-losesWidth)-60, 730)
    }

    private fun drawBorder(g: Graphics2D, width: Int, height: Int) {
        g.color = Color.decode("#ff4753")

        g.fillRect(0, 0, width, 24)
        g.fillRect(0, height-24, width, 24)

        g.fillRect(0, 0, 24, height)
        g.fillRect(width-24, 24, 24, height)
    }

}