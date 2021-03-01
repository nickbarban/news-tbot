package com.nb.newstbot.domain;

import com.nb.newstbot.service.NewsParser;
import com.nb.newstbot.service.NewsParserImpl;
import com.nb.newstbot.utils.Utils;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Nick Barban.
 */
public class NewsCommand extends ServiceCommand {

    private NewsParser parser = new NewsParserImpl();

    public NewsCommand(String identifier, String description) {
        super(identifier, description);
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] arguments) {
        String username = Utils.getUsername(user);

        sendMessage(sender, chat, username);

    }

    @Scheduled
    private void sendMessage(AbsSender sender, Chat chat, String username) {
        try {
            List<Article> articles = parser.getNews();
            articles.forEach(a -> {
                String message = prepareMessage(a);
                sendAnswer(sender, chat.getId(), this.getCommandIdentifier(), username, message);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String prepareMessage(Article article) {
        return "\t\t<b>" + article.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</b>\n" + "<a href=\"" + article.getLink() + "\">" + article.getTitle() + "</a>\n\n";
    }
}
