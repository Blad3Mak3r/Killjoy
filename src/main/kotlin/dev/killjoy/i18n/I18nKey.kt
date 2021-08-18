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
    ABILITY_ACTION_BUTTON("ability.actionButton"),
    ABILITY_COST("ability.cost"),
    ABILITY_NOT_FOUND("ability.notFound"),
    VALORANT_ABILITIES_TITLE("valorant.abilities.title"),

    AGENT_CLASS_CONTROLLER("agent.class.controller"),
    AGENT_CLASS_DUELIST("agent.class.duelist"),
    AGENT_CLASS_INITIATOR("agent.class.initiator"),
    AGENT_CLASS_SENTINEL("agent.class.sentinel"),
    AGENT_GENDER_FEMALE("agent.gender.female"),
    AGENT_GENDER_MALE("agent.gender.male"),

    ARSENAL_TYPE_SMGS("arsenal.type.smgs"),
    ARSENAL_TYPE_RIFLES("arsenal.type.rifles"),
    ARSENAL_TYPE_SHOTGUNS("arsenal.type.shotguns"),
    ARSENAL_TYPE_SNIPERS("arsenal.type.snipers"),
    ARSENAL_TYPE_MELEE("arsenal.type.melee"),
    ARSENAL_TYPE_HEAVIES("arsenal.type.heavies"),
    ARSENAL_TYPE_SIDEARMS("arsenal.type.sidearms"),

    ARSENAL_WALLPENETRATION_LOW("arsenal.wallPenetration.low"),
    ARSENAL_WALLPENETRATION_MEDIUM("arsenal.wallPenetration.medium"),
    ARSENAL_WALLPENETRATION_HIGH("arsenal.wallPenetration.high"),

    CONTENT_NOT_FOUND("contentNotFound"),
    CONTENT_NOT_FOUND_DESCRIPTION("contentNotFoundDescription"),

    COMMAND_CANNOT_USE_OUTSIDE_GUILD("commandCannotUseOutsideGuild"),
    COMMAND_NOT_IMPLEMENTED("commandNotImplemented"),

    EXCEPTION_HANDLING_SLASH_COMMAND_OPTION("exceptionHandlingSlashCommandOption"),

    GUILD_JOIN_TITLE("guildJoin.title"),
    GUILD_JOIN_CONTENT("guildJoin.content"),

    NOT_AVAILABLE_AT_THE_MOMENT("notAvailableAtTheMoment")
}