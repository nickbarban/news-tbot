package com.nb.newstbot.domain;

import com.nb.newstbot.service.InstagramClient;
import com.nb.newstbot.service.NewsParser;
import com.nb.newstbot.service.NewsParserImpl;
import com.nb.newstbot.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.brunocvcunha.instagram4j.Instagram4j;
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

    private final String instaUsername = "nickbrabus_2020";
    private final String instaPassword = "y96S^Qkg(3$DzCw";
    private NewsParser parser = new NewsParserImpl();
    private Map<Long, NewsMetaData> latestArticlePerChat = new HashMap<>();
    private InstagramClient instagram = new InstagramClient();

    public NewsCommand(String identifier, String description) {
        super(identifier, description);
        NewsMetaData instagramMetaData = new NewsMetaData();
        Long instagramChatId = -1L;
        latestArticlePerChat.put(instagramChatId, instagramMetaData);
        final Instagram4j instagram4j = instagram.initInstagram(instaUsername, instaPassword);
//        instagram.loginInstagram(instagram4j);
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] arguments) {
        saveIfNotExists(chat, user);
        String username = Utils.getUsername(user);
        sendMessage(sender, chat, username);

    }

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
                        log.debug("Send message: {} to chat {}", message, chatId);
                        // TODO by nbarban: 09/03/21 Should be added possibility to send unread articles to each chat personally
                        if (chatId > 0) {
                            sendAnswer(sender, chatId, commandIdentifier, username, message);
                        } else {
                            sendToInstagram(message);
                        }
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

    private void sendToInstagram(String message) {
        instagram.send(message);
    }

    private List<Article> getArticles(Chat chat) {
        try {
            return parser.getNews();
        } catch (IOException ex) {
            String error = "Could not parse and sent news to chat %d".formatted(chat.getId());
            log.error(error, ex);
        }
        return Collections.emptyList();
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
