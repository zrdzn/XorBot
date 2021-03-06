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
package io.github.zrdzn.bot.xorbot.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandListener extends ListenerAdapter {

    private final CommandRegistry commandRegistry;
    private final boolean testBuild;

    public CommandListener(CommandRegistry commandRegistry, boolean testBuild) {
        this.commandRegistry = commandRegistry;
        this.testBuild = testBuild;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // If test build is true and channel does not equal test channel id, cancel
        if (event.getChannel().getId().equalsIgnoreCase("872881918616686696")) {
            if (!this.testBuild) {
                return;
            }
        } else {
            if (this.testBuild) {
                return;
            }
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getMember() == null) {
            return;
        }

        String rawContent = event.getMessage().getContentRaw();

        if (!rawContent.startsWith("!")) {
            return;
        }

        List<String> optionList = new ArrayList<>(Arrays.asList(rawContent.split(" ")));

        String commandName = optionList.get(0).substring(1);
        if (commandName.isBlank()) {
            return;
        }

        Map<String, Command> commandMap = this.commandRegistry.getCommands();
        if (!commandMap.containsKey(commandName)) {
            event.getChannel().sendMessage("Provided command does not exist in the command registry. Check !help for the command list.").queue();
            return;
        }

        optionList.remove(0);

        CompletableFuture.runAsync(() -> commandMap.get(commandName).execute(event, optionList));
    }

}
