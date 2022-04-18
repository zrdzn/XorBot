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

import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class XorPunishmentService implements PunishmentService {

    public static final String DEFAULT_REASON = "Broken rules.";

    private final Logger logger;
    private final List<Punishment> punishments;
    private final PunishmentRepository punishmentRepository;

    public XorPunishmentService(Logger logger, PunishmentRepository punishmentRepository) {
        this.logger = logger;
        this.punishments = new ArrayList<>();
        this.punishmentRepository = punishmentRepository;
    }

    public void loadPunishments() {
        this.punishments.addAll(this.punishmentRepository.list());
    }

    @Override
    public CompletableFuture<Optional<Punishment>> createPunishment(long targetId, String targetName, PunishmentType type,
                                                                    long executorId, String executorName, Duration duration) {
        return this.createPunishment(targetId, targetName, type, executorId, executorName, DEFAULT_REASON, duration);
    }

    @Override
    public CompletableFuture<Optional<Punishment>> createPunishment(long targetId, String targetName, PunishmentType type, long executorId,
                                                                 String executorName, String reason, Duration duration) {
        return CompletableFuture.supplyAsync(() -> {
            String finalReason = reason;

            if (finalReason == null) {
                finalReason = DEFAULT_REASON;
            }

            if (!this.punishmentRepository.save(targetId, targetName, type, executorId, executorName, finalReason, duration)) {
                this.logger.error("Could not save new punishment to the repository.");
                return Optional.empty();
            }

            this.punishmentRepository.findByTargetId(targetId, type).ifPresent(this.punishments::add);

            return this.punishments.stream()
                .filter(punishment -> punishment.getTargetId() == targetId)
                .filter(punishment -> punishment.getType() == type)
                .findAny();
        });
    }

    @Override
    public CompletableFuture<Void> removePunishment(long targetId, PunishmentType type) {
        return CompletableFuture.runAsync(() -> {
            if (!this.punishmentRepository.deleteByTargetId(targetId, type)) {
                return;
            }

            this.punishments.removeIf(punishment -> punishment.getTargetId() == targetId);
        });
    }

    @Override
    public CompletableFuture<Optional<Punishment>> getPunishment(long targetId, PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Punishment> punishmentMaybe = this.punishments.stream()
                .filter(punishment -> punishment.getTargetId() == targetId)
                .filter(punishment -> punishment.getType() == type)
                .findAny();
            if (punishmentMaybe.isPresent()) {
                return punishmentMaybe;
            }

            return this.punishmentRepository.findByTargetId(targetId, type);
        });
    }

    @Override
    public CompletableFuture<Boolean> punishmentExists(long targetId, PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.punishments.stream().noneMatch(punishment -> punishment.getTargetId() == targetId && punishment.getType() == type)) {
                Optional<Punishment> punishmentMaybe = this.punishmentRepository.findByTargetId(targetId, type);
                if (punishmentMaybe.isPresent()) {
                    this.punishments.add(punishmentMaybe.get());
                    return true;
                }

                return false;
            }

            return true;
        });
    }

    public List<Punishment> getCachedPunishments() {
        return this.punishments;
    }

}
