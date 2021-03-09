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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Nick Barban.
 */
@Slf4j
public class NewsCommand extends ServiceCommand {

    private NewsParser parser = new NewsParserImpl();
    private Article latest = null;
    private Map<Long, NewsMetaData> latestArticlePerChat = new HashMap<>();

    public NewsCommand(String identifier, String description) {
        super(identifier, description);
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] arguments) {
        saveIfNotExists(chat, user);
        String username = Utils.getUsername(user);
        sendMessage(sender, chat, username);

    }

    @Scheduled
    private void sendMessage(AbsSender sender, Chat chat, String username) {
        final String commandIdentifier = this.getCommandIdentifier();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final List<Article> articles = new ArrayList<>();
                latestArticlePerChat.forEach((chatId, metaData) -> {
                    try {

                        if (newChat(chatId)) {
                            articles.addAll(parser.getNews());
                        } else {
                            articles.addAll(parser.getLatestNews(metaData.getLatestArticle()));
                        }

                        metaData.setLatestArticle(articles.get(articles.size() - 1));
                        log.info("{} messages will be sent", articles.size());
                        articles.forEach(a -> {
                            String message = prepareMessage(a);
                            log.info("Send message: {} to {} chats", message, latestArticlePerChat.size());
                            // TODO by nbarban: 09/03/21 Should be added possibility to send unread articles to each chat personally
                            sendAnswer(sender, chatId, commandIdentifier, username, message);
                            latest = articles.get(articles.size() - 1);
                        });
                    } catch (IOException ex) {
                        String error = "Could not parse and sent news to chat %d".formatted(chatId);
                        log.error(error, ex);
                    }
                });
            }
        };
        Timer timer = new Timer();
        final int periodInMilliseconds = 60000 * 5;
        timer.scheduleAtFixedRate(task, new Date(), periodInMilliseconds);
    }

    private boolean newChat(Long chatId) {
        return latestArticlePerChat.get(chatId).getLatestArticle() == null;
    }

    private void saveIfNotExists(Chat chat, User user) {
        NewsMetaData meta = new NewsMetaData();
        meta.setChat(chat);
        meta.setUser(user);
        latestArticlePerChat.put(chat.getId(), meta);
    }

    private String prepareMessage(Article article) {
        return "\t\t<b>" + article.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</b>\n" + "<a href=\"" + article.getLink() + "\">" + article.getTitle() + "</a>\n\n";
    }
}
