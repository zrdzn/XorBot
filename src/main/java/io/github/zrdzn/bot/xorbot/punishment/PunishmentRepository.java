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

import com.zaxxer.hikari.HikariDataSource;
import io.github.zrdzn.bot.xorbot.user.UserCreationException;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PunishmentRepository {

    private final HikariDataSource dataSource;
    private final Logger logger;

    public PunishmentRepository(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
    }

    public boolean save(long targetId, String targetName, PunishmentType type, long executorId,
                        String executorName, String reason, Duration duration) throws UserCreationException {
        long durationMillis = -1L;
        if (duration != null) {
            durationMillis = duration.toMillis();
        }

        String insertQuery = "INSERT INTO punishments (target_id, target_name, type, executor_id, executor_name, reason, duration)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setLong(1, targetId);
            statement.setString(2, targetName);
            statement.setString(3, type.toString());
            statement.setLong(4, executorId);
            statement.setString(5, executorName);
            statement.setString(6, reason);
            statement.setLong(7, durationMillis);

            int affectedRows = statement.executeUpdate();

            return affectedRows == 1;
        } catch (SQLException exception) {
            this.logger.error("Could not insert punishment into database.", exception);
            return false;
        }
    }

    public List<Punishment> list() {
        List<Punishment> punishments = new ArrayList<>();
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments;")) {
            ResultSet result = statement.executeQuery();
            if (result == null) {
                return punishments;
            }

            if (result.next()) {
                punishments.add(XorPunishment.builder()
                    .id(result.getLong("id"))
                    .targetId(result.getLong("target_id"))
                    .targetName(result.getString("target_name"))
                    .type(PunishmentType.valueOf(result.getString("type")))
                    .executorId(result.getLong("executor_id"))
                    .executorName(result.getString("executor_name"))
                    .reason(result.getString("reason"))
                    .duration(Duration.ofMillis(result.getLong("duration")))
                    .build());
            }

            return punishments;
        } catch (SQLException exception) {
            this.logger.error("Could not select punishments from database.", exception);
            return punishments;
        }
    }

    public boolean deleteByTargetId(long targetId, PunishmentType type) {
        if (!this.existsByTargetId(targetId, type)) {
            return false;
        }

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM punishments WHERE target_id = ? AND type = ? LIMIT 1;")) {
            statement.setLong(1, targetId);
            statement.setString(2, type.toString());

            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            this.logger.error("Could not delete punishment from database.", exception);
            return false;
        }
    }

    public Optional<Punishment> findByTargetId(long targetId, PunishmentType type) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments WHERE target_id = ? AND type = ?;")) {
            statement.setLong(1, targetId);
            statement.setString(2, type.toString());

            ResultSet result = statement.executeQuery();
            if (result == null || !result.next()) {
                return Optional.empty();
            }

            return Optional.of(XorPunishment.builder()
                .id(result.getLong("id"))
                .targetId(targetId)
                .targetName(result.getString("target_name"))
                .type(type)
                .executorId(result.getLong("executor_id"))
                .executorName(result.getString("executor_name"))
                .reason(result.getString("reason"))
                .duration(Duration.ofMillis(result.getLong("duration")))
                .build());
        } catch (SQLException exception) {
            this.logger.error("Could not select punishment from database.", exception);
            return Optional.empty();
        }
    }

    public boolean existsByTargetId(long targetId, PunishmentType type) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id FROM punishments WHERE target_id = ? AND type = ?;")){
            statement.setLong(1, targetId);
            statement.setString(2, type.toString());

            return statement.executeQuery().next();
        } catch (SQLException exception) {
            this.logger.error("Could not select punishment from database.", exception);
            return false;
        }
    }

}
