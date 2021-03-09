package com.nb.newstbot.domain;

import com.nb.newstbot.service.NewsParser;
import com.nb.newstbot.service.NewsParserImpl;
import com.nb.newstbot.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Nick Barban.
 */
@Slf4j
public class NewsCommand extends ServiceCommand {

    private NewsParser parser = new NewsParserImpl();
    private Article latest = null;

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
        final String commandIdentifier = this.getCommandIdentifier();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    List<Article> articles;

                    if (latest == null) {
                        articles = parser.getNews();
                    } else {
                        articles = parser.getLatestNews(latest);
                    }
                    log.info("{} messages will be sent", articles.size());
                    articles.forEach(a -> {
                        String message = prepareMessage(a);
                        log.info("Send message: {}", message);
                        sendAnswer(sender, chat.getId(), commandIdentifier, username, message);
                        latest = articles.get(articles.size() - 1);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        final int periodInMilliseconds = 60000 * 5;
        timer.scheduleAtFixedRate(task, new Date(), periodInMilliseconds);
    }

    private String prepareMessage(Article article) {
        return "\t\t<b>" + article.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</b>\n" + "<a href=\"" + article.getLink() + "\">" + article.getTitle() + "</a>\n\n";
    }
}
