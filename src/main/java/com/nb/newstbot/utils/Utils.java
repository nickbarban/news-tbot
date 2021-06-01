package com.nb.newstbot.utils;

import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

/**
 * @author Nick Barban.
 */
public final class Utils {

    private Utils() {
        throw new AssertionError("You can not create instance of class Utils");
    }

    public static String getUsername(User user) {
        return Optional.ofNullable(user.getUserName()).orElse(getFio(user));
    }

    public static String getFio(User user) {
        return String.join(" ", user.getFirstName(), user.getLastName());
    }
}
