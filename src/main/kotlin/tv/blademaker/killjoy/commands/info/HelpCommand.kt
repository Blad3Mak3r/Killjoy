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

package tv.blademaker.killjoy.commands.info

import tv.blademaker.killjoy.*
import tv.blademaker.killjoy.framework.Category
import tv.blademaker.killjoy.framework.ColorExtra
import tv.blademaker.killjoy.framework.annotations.CommandArgument
import tv.blademaker.killjoy.framework.CommandContext
import tv.blademaker.killjoy.framework.abs.Command
import tv.blademaker.killjoy.framework.annotations.CommandProperties

@CommandProperties(
    name = "help",
    category = Category.Information,
    arguments = [
        CommandArgument("command", "The command you want to get information from.", false)
    ])
class HelpCommand : Command() {
    override suspend fun handle(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            ctx.embed {
                setColor(ColorExtra.VAL_BLUE)
                setTitle("Killjoy help")

                addField("Command Usage", "Killjoy is a very easy to use bot, the prefix is __**joy**__ and you can execute a command simply using __**joy commandname**__," +
                        " for example __**joy agents**__ and you can also further explore the possibilities of that command by adding arguments as," +
                        " __**joy agents jett**__ that will show you the information of the Valorant agent that you want.", true)
                addField("Commands with arguments", "In case you need specific information about a command," +
                        " you can use this command adding the name of the command behind as an argument __**joy help commandname**__ for example __**joy help agents**__", false)

                addBlankField(false)
                for (category in Category.values()) {
                    if (!category.isPublic || !category.isEnabled) continue
                    addField(
                            category.name,
                            Launcher.commandRegistry.getCommands(category).joinToString(" ") { "``${it.props.name}``" },
                            true)
                }
                addBlankField(false)
                addField("Useful links", "[Add Bot]($INVITE_URL) | [Website]($WEBSITE_URL) | [Repository]($REPOSITORY_URL) | [Report a bug]($BUG_REPORT_URL)", false)
            }.queue()
        } else {
            val invoke = ctx.args[0]
            val cmd = Launcher.commandRegistry.getCommand(invoke, false) ?: return

            ctx.embed {
                setColor(ColorExtra.VAL_BLUE)
                setAuthor("Killjoy Help")
                setTitle(cmd.props.name.toUpperCase())
                setDescription(cmd.help)
                appendDescription(" [[Read more](${WEBSITE_URL}/commands?command=${cmd.props.name.toLowerCase()})]")
                addField("Usage", buildString {
                    appendLine("joy **${cmd.props.name}** ${buildString { 
                        for (arg in cmd.args) {
                            append(if (arg.isRequired) "(``" else "[``")
                            append(arg.name)
                            append(if (arg.isRequired) "``) " else "``] ")
                        }
                    }}")
                }, false)
            }.queue()
        }
    }

    override val help = "Get help on using the Killjoy commands"
}