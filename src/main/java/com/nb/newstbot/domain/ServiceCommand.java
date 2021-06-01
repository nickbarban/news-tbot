package com.nb.newstbot.domain;


import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @author Nick Barban.
 */
@Slf4j
public abstract class ServiceCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param identifier  the unique identifier of this command (e.g. the command string to
     *                    enter into chat)
     * @param description the description of this command
     */
    ServiceCommand(String identifier, String description) {
        super(identifier, description);
    }

    void sendAnswer(AbsSender sender, Long chatId, String commandName, String username, String text) {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        message.setParseMode(ParseMode.HTML);
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error(String.format("Could not execute command '%s' from %s", commandName, username), e);
        }
    }
}
