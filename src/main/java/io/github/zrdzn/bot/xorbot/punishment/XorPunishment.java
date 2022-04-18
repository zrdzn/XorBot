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
import java.util.Arrays;

public class XorPunishment implements Punishment {

    private long id;
    private long targetId;
    private String targetName;
    private PunishmentType type;
    private long executorId;
    private String executorName;
    private String reason;
    private Duration duration;

    private XorPunishment() {
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public long getTargetId() {
        return this.targetId;
    }

    @Override
    public String getTargetName() {
        return this.targetName;
    }

    @Override
    public PunishmentType getType() {
        return this.type;
    }

    @Override
    public long getExecutorId() {
        return this.executorId;
    }

    @Override
    public String getExecutorName() {
        return this.executorName;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public Duration getDuration() {
        return this.duration;
    }

    public static class Builder {

        private long id;
        private long targetId;
        private String targetName;
        private PunishmentType type;
        private long executorId;
        private String executorName;
        private String reason;
        private Duration duration;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder targetId(long targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder targetName(String targetName) {
            this.targetName = targetName;
            return this;
        }

        public Builder type(PunishmentType type) {
            this.type = type;
            return this;
        }

        public Builder executorId(long executorId) {
            this.executorId = executorId;
            return this;
        }

        public Builder executorName(String executorName) {
            this.executorName = executorName;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public XorPunishment build() {
            if (this.id == 0L) {
                throw new IllegalArgumentException("Punishment's id must be above 0.");
            } else if (String.valueOf(this.targetId).length() < 17) {
                throw new IllegalArgumentException("Target's discord id must be above 17 digit length.");
            } else if (this.targetName == null) {
                throw new IllegalArgumentException("Target's discord name cannot be null.");
            } else if (Arrays.stream(PunishmentType.values()).noneMatch(value -> value == this.type)) {
                throw new IllegalArgumentException("Punishment's type does not match any of available types.");
            } else if (String.valueOf(this.executorId).length() < 17) {
                throw new IllegalArgumentException("Executor's discord id must be above 17 digit length.");
            } else if (this.executorName == null) {
                throw new IllegalArgumentException("Executor's discord name cannot be null.");
            } else if (this.reason == null) {
                throw new IllegalArgumentException("Punishment's reason cannot be null.");
            }

            XorPunishment punishment = new XorPunishment();

            punishment.id = this.id;
            punishment.targetId = this.targetId;
            punishment.targetName = this.targetName;
            punishment.type = this.type;
            punishment.executorId = this.executorId;
            punishment.executorName = this.executorName;
            punishment.reason = this.reason;
            punishment.duration = this.duration;

            return punishment;
        }

    }

    public static XorPunishment.Builder builder() {
        return new XorPunishment.Builder();
    }

}
