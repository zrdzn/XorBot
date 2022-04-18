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
package io.github.zrdzn.bot.xorbot;

import com.google.common.eventbus.EventBus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.zrdzn.bot.xorbot.command.CommandListener;
import io.github.zrdzn.bot.xorbot.command.CommandRegistry;
import io.github.zrdzn.bot.xorbot.command.commands.BanCommand;
import io.github.zrdzn.bot.xorbot.command.commands.BotInformationCommand;
import io.github.zrdzn.bot.xorbot.command.commands.HelpCommand;
import io.github.zrdzn.bot.xorbot.command.commands.MoneyCommand;
import io.github.zrdzn.bot.xorbot.command.commands.SlowmodeCommand;
import io.github.zrdzn.bot.xorbot.economy.EconomyRepository;
import io.github.zrdzn.bot.xorbot.economy.EconomyService;
import io.github.zrdzn.bot.xorbot.economy.XorEconomyService;
import io.github.zrdzn.bot.xorbot.log.LogListener;
import io.github.zrdzn.bot.xorbot.punishment.PunishmentRepository;
import io.github.zrdzn.bot.xorbot.punishment.XorPunishmentService;
import io.github.zrdzn.bot.xorbot.user.UserRepository;
import io.github.zrdzn.bot.xorbot.user.XorUserService;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class XorBot {

    public static void main(String[] args) throws LoginException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Token was not provided.");
        }

        boolean testBuild = args.length == 2 && args[1].equalsIgnoreCase("dev");

        JDABuilder jdaBuilder = JDABuilder.createDefault(args[0]);

        XorBot app = new XorBot();
        app.run(jdaBuilder, testBuild);
    }

    public void run(JDABuilder jdaBuilder, boolean testBuild) throws LoginException {
        BasicConfigurator.configure();
        Logger logger = JDALogger.getLog("DISCORD-BOT");

        String databaseConfig = testBuild ? "test_database" : "database";
        HikariDataSource dataSource = new HikariDataSource(new HikariConfig("/" + databaseConfig + ".properties"));

        try (Connection connection = dataSource.getConnection()) {
            String usersQuery = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "discord_id VARCHAR(20) NOT NULL UNIQUE KEY," +
                "username VARCHAR(32) NOT NULL," +
                "balance BIGINT UNSIGNED DEFAULT 0);";

            PreparedStatement usersUpdate = connection.prepareStatement(usersQuery);

            logger.info("Checking if table 'users' exist...");

            if (usersUpdate.executeUpdate() == 0) {
                logger.info("Table 'users' exists, skipping...");
            } else {
                logger.info("Created new table 'users'.");
            }

            usersUpdate.closeOnCompletion();

            String punishmentsQuery = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "target_id VARCHAR(20) NOT NULL," +
                "target_name VARCHAR(32) NOT NULL," +
                "type VARCHAR(16) NOT NULL," +
                "executor_id VARCHAR(20) NOT NULL," +
                "executor_name VARCHAR(32) NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "duration LONG);";

            PreparedStatement punishmentsUpdate = connection.prepareStatement(punishmentsQuery);

            logger.info("Checking if table 'punishments' exist...");

            if (punishmentsUpdate.executeUpdate() == 0) {
                logger.info("Table 'punishments' exists, skipping...");
            } else {
                logger.info("Created new table 'punishments'.");
            }

            punishmentsUpdate.closeOnCompletion();
        } catch (SQLException exception) {
            logger.error("Could not create-if-not-exists table 'users' or 'punishments'. Something went wrong.", exception);
            return;
        }

        CommandRegistry commandRegistry = new CommandRegistry();

        XorUserService userService = new XorUserService(new UserRepository(dataSource, logger));
        userService.loadUsers();
        logger.info("Loaded {} users from the database.", userService.getCachedUsers().size());

        XorPunishmentService punishmentService = new XorPunishmentService(logger, new PunishmentRepository(dataSource, logger));
        punishmentService.loadPunishments();
        logger.info("Loaded {} punishments from the database.", punishmentService.getCachedPunishments().size());

        EconomyService economyService = new XorEconomyService(new EconomyRepository(dataSource, logger));

        logger.info("Registering default commands...");
        commandRegistry.register(new HelpCommand(commandRegistry));
        commandRegistry.register(new MoneyCommand(userService, economyService));
        commandRegistry.register(new SlowmodeCommand());
        commandRegistry.register(new BotInformationCommand(commandRegistry));
        commandRegistry.register(new BanCommand(commandRegistry, punishmentService));
        logger.info("Registered all default commands.");

        logger.info("Initializing event bus...");
        EventBus eventBus = new EventBus("LogListener-EventBus");

        logger.info("Reading bot configuration file...");
        Properties configuration = new Properties();
        String fileName = "xorbot.config";
        try (FileInputStream inputStream = new FileInputStream(fileName)) {
            configuration.load(inputStream);
        } catch (IOException ex) {
            logger.error("Could not read configuration file.");
            return;
        }

        long logChannelId;
        try {
            logChannelId = Long.parseLong(configuration.getProperty("channel_log_id"));
        } catch (NumberFormatException exception) {
            logger.error("channel_log_id is not a valid long number.");
            return;
        }
        logger.info("Using channel with id {} as log channel.", logChannelId);

        logger.info("Registering listeners...");
        jdaBuilder.addEventListeners(new CommandListener(commandRegistry, testBuild),
            new LogListener(eventBus, logChannelId)).build();
        logger.info("Registered all listeners. JDA Built, ready to go.");
    }

}
