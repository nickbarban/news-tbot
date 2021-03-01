package com.nb.newstbot.domain;

import com.nb.newstbot.NewsTbotApplication;
import com.nb.newstbot.utils.Utils;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * @author Nick Barban.
 */

public class HelpCommand extends ServiceCommand {

    public HelpCommand(String identifier, String description) {
        super(identifier, description);
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] arguments) {
        String username = Utils.getUsername(user);
        String message = "This bot will send you news from next resources: %s".formatted(NewsTbotApplication.RESOURCES);
        sendAnswer(sender, chat.getId(), this.getCommandIdentifier(), username, message);
    }
}
