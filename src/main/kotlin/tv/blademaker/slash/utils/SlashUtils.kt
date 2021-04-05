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

package tv.blademaker.slash.utils

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.slf4j.LoggerFactory
import tv.blademaker.slash.api.AbstractSlashCommand
import java.lang.IllegalStateException
import java.lang.reflect.Modifier

object SlashUtils {

    private val LOGGER = LoggerFactory.getLogger(SlashUtils::class.java)

    fun discoverSlashCommands(packageName: String): List<AbstractSlashCommand> {
        val classes = Reflections(packageName, SubTypesScanner())
            .getSubTypesOf(AbstractSlashCommand::class.java)
            .filter { !Modifier.isAbstract(it.modifiers) && AbstractSlashCommand::class.java.isAssignableFrom(it) }

        LOGGER.info("Discovered a total of ${classes.size} slash commands in package $packageName")

        val commands = mutableListOf<AbstractSlashCommand>()

        for (clazz in classes) {
            val instance = clazz.getDeclaredConstructor().newInstance()
            val commandName = instance.commandName.toLowerCase()

            if (commands.any { it.commandName.equals(commandName, true) }) {
                throw IllegalStateException("Command with name $commandName is already registered.")
            }

            commands.add(instance)
        }

        return commands
    }
}