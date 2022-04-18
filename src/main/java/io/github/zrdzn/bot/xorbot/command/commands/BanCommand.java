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
package io.github.zrdzn.bot.xorbot.command.commands;

import io.github.zrdzn.bot.xorbot.command.Command;
import io.github.zrdzn.bot.xorbot.command.CommandRegistry;
import io.github.zrdzn.bot.xorbot.embed.EmbedHelper;
import io.github.zrdzn.bot.xorbot.punishment.PunishmentService;
import io.github.zrdzn.bot.xorbot.punishment.PunishmentType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

public class BanCommand implements Command {

    private final CommandRegistry commandRegistry;
    private final PunishmentService punishmentService;

    public BanCommand(CommandRegistry commandRegistry, PunishmentService punishmentService) {
        this.commandRegistry = commandRegistry;
        this.punishmentService = punishmentService;
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Bans user with specified reason.");
    }

    @Override
    public Optional<String> getUsage() {
        return Optional.of(String.format("!%s <user> [reason]", this.getName()));
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> optionList) {
        TextChannel channel = event.getTextChannel();

        Member member = event.getMember();
        if (!member.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessageEmbeds(EmbedHelper.NO_PERMISSIONS_EMBED).queue();
            return;
        }

        if (optionList.isEmpty()) {
            this.getUsage().ifPresent(usage -> channel.sendMessage(usage).queue());
            return;
        }

        Member target = event.getMessage().getMentionedMembers().get(0);
        if (target == null) {
            channel.sendMessageEmbeds(EmbedHelper.NO_MENTIONED_USER).queue();
            return;
        }

        MessageEmbed reply = EmbedHelper.moderation(member.getUser())
            .setDescription("User has been successfully banned.")
            .build();

        String reason;
        try {
            reason = optionList.get(1);
        } catch (IndexOutOfBoundsException exception) {
            reason = null;
        }

        String finalReason = reason;
        this.punishmentService.createPunishment(target.getIdLong(), target.getUser().getName(), PunishmentType.BAN,
                member.getIdLong(), member.getUser().getName(), finalReason, null)
            .thenAccept(punishment -> {
                if (punishment.isEmpty()) {
                    channel.sendMessageEmbeds(EmbedHelper.COULD_NOT_PUNISH).queue();
                    return;
                }

                target.ban(0, finalReason).queue();
                channel.sendMessageEmbeds(reply).queue();
            });
    }

}