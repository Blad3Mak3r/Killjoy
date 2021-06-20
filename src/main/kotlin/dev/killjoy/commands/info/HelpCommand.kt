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

package dev.killjoy.commands.info

import dev.killjoy.Launcher
import dev.killjoy.WEBSITE_URL
import dev.killjoy.framework.Category
import dev.killjoy.framework.ColorExtra
import dev.killjoy.framework.CommandContext
import dev.killjoy.framework.abs.Command
import dev.killjoy.framework.annotations.CommandArgument
import dev.killjoy.framework.annotations.CommandProperties

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

                addField("Slash Commands", "Slash Commands have been available in Killjoy for some time, " +
                        "they are very intuitive and easy to use, just by putting ``/`` in a text chat, a menu will " +
                        "appear with all the available commands, if this menu does not come out or not You get the " +
                        "Killjoy commands, you must activate the Slash Commands and use the new Killjoy invitation link.", false)
                addField("Command Usage", "Killjoy is a very easy to use bot, the prefix is __**joy**__ and you can execute a command simply using __**joy commandname**__," +
                        " for example __**joy agents**__ and you can also further explore the possibilities of that command by adding arguments as," +
                        " __**joy agents jett**__ that will show you the information of the Valorant agent that you want.", false)
                addField("Commands with arguments", "In case you need specific information about a command," +
                        " you can use this command adding the name of the command behind as an argument __**joy help commandname**__ for example __**joy help agents**__", false)
                addField("Vote for Killjoy", "If you like **Killjoy** and its content, don't forget to show your " +
                        "support, by **voting for Killjoy on top.gg**, you can vote every 12 hours and for us it is a great " +
                        "show of support from you.", false)

                addBlankField(false)
                for (category in Category.values()) {
                    if (!category.isPublic || !category.isEnabled) continue
                    addField(
                            category.name,
                            Launcher.commandRegistry.getCommands(category).joinToString(" ") { "``${it.props.name}``" },
                            true)
                }
                addBlankField(false)
            }.setActionRows(HelpSlashCommand.ACTION_ROWS).queue()
        } else {
            val invoke = ctx.args[0]
            val cmd = Launcher.commandRegistry.getCommand(invoke, false) ?: return

            ctx.embed {
                setColor(ColorExtra.VAL_BLUE)
                setAuthor("Killjoy Help")
                setTitle(cmd.props.name.uppercase())
                setDescription(cmd.help)
                appendDescription(" [[Read more]($WEBSITE_URL/commands?command=${cmd.props.name.lowercase()})]")
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