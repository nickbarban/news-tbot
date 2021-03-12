package com.nb.newstbot.domain;

import com.nb.newstbot.service.NewsParser;
import com.nb.newstbot.service.NewsParserImpl;
import com.nb.newstbot.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
                List<Article> articles = getArticles(chat);
                latestArticlePerChat.forEach((chatId, metaData) -> {

                    Article latestArticle = metaData.getLatestArticle();
                    List<Article> unsentArticles;

                    if (latestArticle != null) {
                        int latestArticleIndex = articles.indexOf(latestArticle);
                        unsentArticles = articles.subList(latestArticleIndex + 1, articles.size());
                    } else {
                        unsentArticles = articles;
                    }

                    log.info("{} messages will be sent", unsentArticles.size());
                    unsentArticles.forEach(a -> {
                        String message = prepareMessage(a);
                        log.info("Send message: {} to {} chat{}", message, latestArticlePerChat.size(), latestArticlePerChat.size() == 1 ? "" : "s");
                        // TODO by nbarban: 09/03/21 Should be added possibility to send unread articles to each chat personally
                        sendAnswer(sender, chatId, commandIdentifier, username, message);
                    });

                    if (!CollectionUtils.isEmpty(articles)) {
                        metaData.setLatestArticle(articles.get(articles.size() - 1));
                    }
                });
            }
        };
        Timer timer = new Timer();
        final int periodInMilliseconds = 60000 * 5;
        timer.scheduleAtFixedRate(task, new Date(), periodInMilliseconds);
    }

    private List<Article> getArticles(Chat chat) {
        try {
            return parser.getNews();
            /*if (newChat(chat.getId())) {
                return parser.getNews();
            } else {
                NewsMetaData metaData = latestArticlePerChat.get(chat.getId());
                return parser.getLatestNews(metaData.getLatestArticle());
            }*/
        } catch (IOException ex) {
            String error = "Could not parse and sent news to chat %d".formatted(chat.getId());
            log.error(error, ex);
        }
        return Collections.emptyList();
    }

    private boolean newChat(Long chatId) {
        return latestArticlePerChat.get(chatId).getLatestArticle() == null;
    }

    private void saveIfNotExists(Chat chat, User user) {
        if (!latestArticlePerChat.containsKey(chat.getId())) {
            log.info("Add new chat with id: {} for user: {}", chat.getId(), Utils.getUsername(user));
            NewsMetaData meta = new NewsMetaData();
            meta.setChat(chat);
            meta.setUser(user);
            latestArticlePerChat.put(chat.getId(), meta);
        }
    }

    private String prepareMessage(Article article) {
        return "\t\t<b>" + article.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</b>\n" + "<a href=\"" + article.getLink() + "\">" + article.getTitle() + "</a>\n\n";
    }
}
