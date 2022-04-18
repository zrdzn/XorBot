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
package io.github.zrdzn.bot.xorbot.embed;

import io.github.zrdzn.bot.xorbot.log.LogAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.Date;

public class EmbedHelper {

    public static final MessageEmbed NO_PERMISSIONS_EMBED = error().setDescription("No permissions.").build();
    public static final MessageEmbed NO_MENTIONED_USER = error().setDescription("You need to mention someone that is on this server.").build();
    public static final MessageEmbed COULD_NOT_PUNISH = error().setDescription("Something went wrong while punishing this user.").build();

    public static EmbedBuilder info(User executor) {
        return getEmbed(executor, EmbedType.INFORMATION);
    }

    public static EmbedBuilder moderation(User executor) {
        return getEmbed(executor, EmbedType.MODERATION);
    }

    public static EmbedBuilder error() {
        return getEmbed(null, EmbedType.ERROR);
    }

    public static EmbedBuilder log(LogAction logAction) {
        return getEmbed(null, EmbedType.LOG)
            .addField("Action", logAction.getDescription(), true)
            .addField("Date", new Date(System.currentTimeMillis()).toString(), true);
    }

    public static EmbedBuilder getEmbed(User executor, EmbedType type) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (executor != null) {
            embedBuilder.setImage(executor.getAvatarUrl());
        }

        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.setColor(type.getColor());

        return embedBuilder;
    }

    public static String formatUser(User user) {
        return String.format("%s#%s (%s)", user.getName(), user.getDiscriminator(), user.getId());
    }

}
