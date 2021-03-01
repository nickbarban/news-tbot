package com.nb.newstbot.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Nick Barban.
 */
@Data
public class Article {
    private String link;
    private LocalDateTime date;
    private String title;
}
