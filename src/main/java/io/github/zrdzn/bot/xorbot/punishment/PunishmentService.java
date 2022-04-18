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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PunishmentService {

    /**
     * Creates new punishment with default reason and adds it to the database.
     *
     * @param targetId a discord id of the target
     * @param targetName a discord name of the target
     * @param type a type of the punishment
     * @param executorId a discord id of the executor
     * @param executorName a discord name of the executor
     * @param duration a duration of the punishment
     *
     * @return newly created punishment
     */
    CompletableFuture<Optional<Punishment>> createPunishment(long targetId, String targetName, PunishmentType type,
                                                             long executorId, String executorName, Duration duration);

    /**
     * Creates new punishment and adds it to the database.
     *
     * @param targetId a discord id of the target
     * @param targetName a discord name of the target
     * @param type a type of the punishment
     * @param executorId a discord id of the executor
     * @param executorName a discord name of the executor
     * @param reason a reason of the punishment
     * @param duration a duration of the punishment
     *
     * @return newly created punishment
     */
    CompletableFuture<Optional<Punishment>> createPunishment(long targetId, String targetName, PunishmentType type, long executorId,
                                                             String executorName, String reason, Duration duration);

    /**
     * Removes punishment by target id from the database.
     *
     * @param targetId a discord id of the target
     * @param type a type of the punishment
     */
    CompletableFuture<Void> removePunishment(long targetId, PunishmentType type);

    /**
     * Gets the optional punishment by target id from the database.
     *
     * @param targetId a discord id of the target
     * @param type a type of the punishment
     *
     * @return optional punishment
     */
    CompletableFuture<Optional<Punishment>> getPunishment(long targetId, PunishmentType type);

    /**
     * Gets the boolean if punishment exists by target id in the database or not.
     *
     * @param targetId a discord id of the target
     * @param type a type of the punishment
     *
     * @return true if punishment exists
     */
    CompletableFuture<Boolean> punishmentExists(long targetId, PunishmentType type);

}
