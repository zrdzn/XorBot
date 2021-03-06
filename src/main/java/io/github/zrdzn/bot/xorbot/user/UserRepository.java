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
package io.github.zrdzn.bot.xorbot.user;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final HikariDataSource dataSource;
    private final Logger logger;

    public UserRepository(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
    }

    public boolean save(long discordId, String username, long balance) throws UserCreationException {
        if (this.existsByDiscordId(discordId)) {
            return false;
        }

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO users (discord_id, username, balance) VALUES (?, ?, ?);",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, discordId);
            statement.setString(2, username);
            statement.setLong(3, balance);

            int affectedRows = statement.executeUpdate();

            return affectedRows == 1;
        } catch (SQLException exception) {
            this.logger.error("Could not insert user into database.", exception);
            throw new UserCreationException(discordId, username, "Something went wrong while querying the database.");
        }
    }

    public List<User> list() {
        List<User> users = new ArrayList<>();
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users;")) {
            ResultSet result = statement.executeQuery();
            if (result == null) {
                return users;
            }

            if (result.next()) {
                users.add(XorUser.builder()
                    .id(result.getLong("id"))
                    .discordId(result.getLong("discord_id"))
                    .username(result.getString("username"))
                    .balance(result.getLong("balance"))
                    .build());
            }

            return users;
        } catch (SQLException exception) {
            this.logger.error("Could not select user from database.", exception);
            return users;
        }
    }

    public boolean deleteByDiscordId(long discordId) {
        if (!this.existsByDiscordId(discordId)) {
            return false;
        }

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE discord_id = ? LIMIT 1;")) {
            statement.setLong(1, discordId);

            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            this.logger.error("Could not delete user from database.", exception);
            return false;
        }
    }

    public Optional<User> findByDiscordId(long discordId) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE discord_id = ?;")) {
            statement.setLong(1, discordId);

            ResultSet result = statement.executeQuery();
            if (result == null || !result.next()) {
                return Optional.empty();
            }

            return Optional.of(XorUser.builder()
                    .id(result.getLong("id"))
                    .discordId(discordId)
                    .username(result.getString("username"))
                    .balance(result.getLong("balance"))
                    .build());
        } catch (SQLException exception) {
            this.logger.error("Could not select user from database.", exception);
            return Optional.empty();
        }
    }

    public boolean existsByDiscordId(long discordId) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE discord_id = ?;")){
            statement.setLong(1, discordId);

            return statement.executeQuery().next();
        } catch (SQLException exception) {
            this.logger.error("Could not select user from database.", exception);
            return false;
        }
    }

}
