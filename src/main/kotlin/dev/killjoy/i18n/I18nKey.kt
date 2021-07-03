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

package dev.killjoy.i18n

enum class I18nKey(
    internal val pattern: String
) {
    ABILITY_COST("ability.cost"),
    AGENT_CLASS_CONTROLLER("agent.class.controller"),
    AGENT_CLASS_DUELIST("agent.class.duelist"),
    AGENT_CLASS_INITIATOR("agent.class.initiator"),
    AGENT_CLASS_SENTINEL("agent.class.sentinel"),
    CONTENT_NOT_FOUND("contentNotFound"),
    CONTENT_NOT_FOUND_DESCRIPTION("contentNotFoundDescription"),
    COMMAND_CANNOT_USE_OUTSIDE_GUILD("commandCannotUseOutsideGuild"),
    EXCEPTION_HANDLING_SLASH_COMMAND_OPTION("exceptionHandlingSlashCommandOption"),
    NOT_AVAILABLE_AT_THE_MOMENT("notAvailableAtTheMoment")
}