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
package io.github.zrdzn.bot.xorbot.log;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.github.zrdzn.bot.xorbot.cache.MessageCache;
import io.github.zrdzn.bot.xorbot.embed.EmbedHelper;
import io.github.zrdzn.bot.xorbot.event.events.GuildMemberMuteEvent;
import io.github.zrdzn.bot.xorbot.event.events.GuildMemberUnmuteEvent;
import io.github.zrdzn.bot.xorbot.event.events.GuildMemberWarnAddEvent;
import io.github.zrdzn.bot.xorbot.event.events.GuildMemberWarnRemoveEvent;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LogListener extends ListenerAdapter {

    private final MessageCache cachedMessages;
    private final long logChannelId;

    public LogListener(EventBus eventBus, long logChannelId) {
        this.cachedMessages = new MessageCache(5);
        this.logChannelId = logChannelId;

        eventBus.register(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getMember() == null) {
            return;
        }

        this.cachedMessages.store(event.getMessage());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        MessageChannel logChannel = event.getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_JOIN)
            .addField("Member", EmbedHelper.formatUser(event.getUser()), false)
            .build()).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        MessageChannel logChannel = event.getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        event.getGuild().retrieveAuditLogs()
            .type(ActionType.KICK)
            .limit(1)
            .queue(entries -> {
                Optional<AuditLogEntry> entryMaybe = entries.stream()
                    .filter(entry -> entry.getTargetId().equals(event.getUser().getId()))
                    .findFirst();
                if (entryMaybe.isEmpty()) {
                    logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_LEAVE)
                        .addField("Member", EmbedHelper.formatUser(event.getUser()), false)
                        .build()).queue();
                    return;
                }

                AuditLogEntry entry = entryMaybe.get();

                logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_KICK)
                    .addField("Member", EmbedHelper.formatUser(event.getUser()), false)
                    .addField("Executor", EmbedHelper.formatUser(entry.getUser()), false)
                    .addField("Reason", entry.getReason(), false)
                    .build()).queue();
            });
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        MessageChannel logChannel = event.getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        Optional<Message> messageMaybe = this.cachedMessages.find(storedMessage ->
            storedMessage.getId().equals(event.getMessageId()));
        if (messageMaybe.isEmpty()) {
            return;
        }

        Message message = messageMaybe.get();

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MESSAGE_DELETE)
            .addField("Member", EmbedHelper.formatUser(message.getAuthor()), false)
            .addField("Message", message.getContentRaw(), false)
            .build()).queue();
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        MessageChannel logChannel = event.getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        Optional<Message> messageMaybe = this.cachedMessages.find(storedMessage ->
            storedMessage.getId().equals(event.getMessageId()));
        if (messageMaybe.isEmpty()) {
            return;
        }

        Message message = messageMaybe.get();

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MESSAGE_EDIT)
            .addField("Member", EmbedHelper.formatUser(event.getAuthor()), false)
            .addField("Old message", message.getContentRaw(), false)
            .addField("New message", event.getMessage().getContentRaw(), false)
            .build()).queue();
    }

    @Subscribe
    public void onGuildMemberWarnAdd(@NotNull GuildMemberWarnAddEvent event) {
        MessageChannel logChannel = event.getTarget().getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_WARN_ADD)
            .addField("Member", EmbedHelper.formatUser(event.getTarget().getUser()), false)
            .addField("Executor", EmbedHelper.formatUser(event.getExecutor().getUser()), false)
            .addField("Reason", event.getReason(), false)
            .build()).queue();
    }

    @Subscribe
    public void onGuildMemberWarnRemove(@NotNull GuildMemberWarnRemoveEvent event) {
        MessageChannel logChannel = event.getTarget().getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_WARN_REMOVE)
            .addField("Member", EmbedHelper.formatUser(event.getTarget().getUser()), false)
            .addField("Executor", EmbedHelper.formatUser(event.getExecutor().getUser()), false)
            .build()).queue();
    }

    @Subscribe
    public void onGuildMemberMute(@NotNull GuildMemberMuteEvent event) {
        MessageChannel logChannel = event.getTarget().getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_MUTE)
            .addField("Member", EmbedHelper.formatUser(event.getTarget().getUser()), false)
            .addField("Executor", EmbedHelper.formatUser(event.getExecutor().getUser()), false)
            .addField("Reason", event.getReason(), false)
            .addField("Duration", event.getDurationString(), false)
            .build()).queue();
    }

    @Subscribe
    public void onGuildMemberUnmute(@NotNull GuildMemberUnmuteEvent event) {
        MessageChannel logChannel = event.getTarget().getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_UNMUTE)
            .addField("Member", EmbedHelper.formatUser(event.getTarget().getUser()), false)
            .addField("Executor", EmbedHelper.formatUser(event.getExecutor().getUser()), false)
            .build()).queue();
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        MessageChannel logChannel = event.getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        event.getGuild().retrieveAuditLogs()
            .type(ActionType.BAN)
            .limit(1)
            .queue(entries -> {
                Optional<AuditLogEntry> entryMaybe = entries.stream()
                    .filter(entry -> entry.getTargetId().equals(event.getUser().getId()))
                    .findFirst();
                if (entryMaybe.isEmpty()) {
                    return;
                }

                AuditLogEntry entry = entryMaybe.get();

                logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_BAN)
                    .addField("Member", EmbedHelper.formatUser(event.getUser()), false)
                    .addField("Executor", EmbedHelper.formatUser(entry.getUser()), false)
                    .addField("Reason", entry.getReason(), false)
                    .build()).queue();
            });
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        MessageChannel logChannel = event.getGuild().getTextChannelById(this.logChannelId);
        if (logChannel == null) {
            return;
        }

        event.getGuild().retrieveAuditLogs()
            .type(ActionType.UNBAN)
            .limit(1)
            .queue(entries -> {
                Optional<AuditLogEntry> entryMaybe = entries.stream()
                    .filter(entry -> entry.getTargetId().equals(event.getUser().getId()))
                    .findFirst();
                if (entryMaybe.isEmpty()) {
                    return;
                }

                AuditLogEntry entry = entryMaybe.get();

                logChannel.sendMessageEmbeds(EmbedHelper.log(LogAction.MEMBER_UNBAN)
                    .addField("Member", EmbedHelper.formatUser(event.getUser()), false)
                    .addField("Executor", EmbedHelper.formatUser(entry.getUser()), false)
                    .build()).queue();
            });
    }

}
