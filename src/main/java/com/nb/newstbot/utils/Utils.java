package com.nb.newstbot.utils;

import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

/**
 * @author Nick Barban.
 */

public class Utils {
    public static String getUsername(User user) {
        return Optional.ofNullable(user.getUserName())
                .orElse("%s %s".formatted(user.getFirstName(), user.getLastName()));
    }
}
