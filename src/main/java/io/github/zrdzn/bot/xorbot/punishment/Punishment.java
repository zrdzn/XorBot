/*
 * Copyright (c) 2022 zrdzn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.zrdzn.bot.xorbot.punishment;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public interface Punishment {

    /**
     * Gets the database record id, could be used as
     * unique identifier of the Punishment.
     *
     * @return id of the punishment
     */
    long getId();

    /**
     * Gets the discord id of the target.
     *
     * @return discord id of the target
     */
    long getTargetId();

    /**
     * Gets the name of the target.
     *
     * @return name of the target
     */
    String getTargetName();

    /**
     * Gets the type of the punishment.
     *
     * @return type of the punishment
     */
    PunishmentType getType();

    /**
     * Gets the discord id of the target.
     *
     * @return discord id of the target
     */
    long getExecutorId();

    /**
     * Gets the name of the target.
     *
     * @return name of the target
     */
    String getExecutorName();

    /**
     * Gets the reason of the punishment.
     *
     * @return reason of the punishment
     */
    String getReason();

    /**
     * Gets the duration of the punishment.
     *
     * @return duration of the punishment
     */
    @Nullable
    Duration getDuration();

}
