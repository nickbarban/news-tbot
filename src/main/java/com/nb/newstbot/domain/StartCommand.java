package com.nb.newstbot.domain;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

/**
 * @author Nick Barban.
 */

public class StartCommand extends ServiceCommand {

    public StartCommand(String identifier, String description) {
        super(identifier, description);
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] arguments) {
        String username = Optional.ofNullable(user.getUserName()).orElse("%s %s".formatted(user.getFirstName(), user.getLastName()));
        String message = "Let's start! For recent news type /news. For help type /help";
        sendAnswer(sender, chat.getId(), this.getCommandIdentifier(), username, message);
    }
}
