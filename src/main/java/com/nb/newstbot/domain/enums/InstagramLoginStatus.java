package com.nb.newstbot.domain.enums;

import lombok.Getter;

/**
 * @author Nick Barban.
 */
@Getter
public enum InstagramLoginStatus {
    CODE("Secrurity code is required"),
    OK("Login success"),
    FAIL("Login failed");

    private final String description;

    InstagramLoginStatus(String description) {
        this.description = description;
    }
}
