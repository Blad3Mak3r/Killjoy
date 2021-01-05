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

package tv.blademaker.killjoy.utils.extensions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member

/*val Member.isBotOwner: Boolean
    get() = Config.owners.any { it == this.id }*/

val Member.isSelf: Boolean
    get() = this.jda.selfUser.id == this.user.id

val Member.isDeafened: Boolean
    get() = this.voiceState!!.isDeafened

val Member.isManager: Boolean
    get() = hasPermission(Permission.MANAGE_SERVER)

val Member.isAdmin: Boolean
    get() = hasPermission(Permission.ADMINISTRATOR)