package com.nb.newstbot.domain;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * @author Nick Barban.
 */
@Data
public class NewsMetaData {
    private Chat chat;
    private User user;
    private Article latestArticle;
}
