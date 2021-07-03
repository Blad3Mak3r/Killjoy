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

package dev.killjoy.commands.game

import dev.killjoy.Launcher
import dev.killjoy.extensions.jda.setDefaultColor
import dev.killjoy.extensions.jda.supportedLocale
import dev.killjoy.framework.Category
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandArgument
import dev.killjoy.framework.annotations.CommandProperties
import dev.killjoy.utils.Emojis
import dev.killjoy.utils.Utils

@CommandProperties(
    name = "skills",
    category = Category.Game,
    aliases = ["skill"],
    arguments = [
        CommandArgument("skill_name", "A Skill name [shock-bolt]", true)
    ])
class SkillCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) return Utils.Commands.replyWrongUsage(ctx, this)

        val skill = Launcher.getAbility(ctx.args.joinToString(" "))
            ?: return ctx.send(Emojis.NoEntry, "I have not been able to find that skill...").queue()

        ctx.reply(skill.asEmbed(ctx.guild)).queue()
    }

    override val help: String = "Get information about a skill from a Valorant agent"
}