package com.nb.newstbot.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Nick Barban.
 */
@Data
public class InstaCredential {
    @NotNull
    private String username;
    @NotNull
    private String password;
}
