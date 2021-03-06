package com.nb.newstbot;

import com.nb.newstbot.domain.Article;
import com.nb.newstbot.domain.Tbot;
import com.nb.newstbot.service.InstagramClient;
import com.nb.newstbot.service.NewsParser;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.assertj.core.api.Assertions;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NewsTbotApplicationTests {

    @Autowired
    private NewsParser parser;

    @Test
    public void contextLoads() {
    }

    @Test
    public void shouldParseNewsSite() throws IOException {
        final List<Article> actual = parser.getNews();

        System.out.println(actual);

        Assertions.assertThat(actual)
                .isNotEmpty();
    }

    @Test
    public void shouldSendToTelegramParsedNews() throws TelegramApiException {
        String botName = "parse_news_bott";
        String botToken = "1689880233:AAHad53D1fmhc9tsd8cmkdUTV6sWM5klLNc";
        Tbot bot = new Tbot(botName, botToken);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
    }

    @Test
    public void shouldParseBessarabiainform() throws IOException {
        String url = NewsTbotApplication.RESOURCES.get(0);
        Document document = Jsoup.connect(url).get();
        final Elements lenta = document.select("div.lenta_holder");
        final Element firstLenta = lenta.first();
        final Elements firstLentDivs = firstLenta.select("div");
        final Element firstDiv = firstLentDivs.first();

        System.out.println(new String(new char[100]).replace('\0', '!'));
        final List<Article> articles = firstLentDivs.stream()
                .filter(div -> div.select("dfn") != null && div.select("dfn").first() != null)
                .map(div -> {
                    Article article = new Article();
                    article.setDate(LocalDateTime.of(LocalDate.now(), LocalTime.parse(div.select("dfn").first().text())));
                    article.setLink(div.select("a").first().attr("href"));
                    article.setTitle(div.select("a").first().text());
                    System.out.println(new String(new char[100]).replace('\0', '!'));
                    System.out.println(div.select("span.fa").text());
                    System.out.println(new String(new char[100]).replace('\0', '!'));
                    return article;
                })
                .collect(Collectors.toList());
        articles.forEach(article -> System.out.println(ReflectionToStringBuilder.toString(article, ToStringStyle.JSON_STYLE)));
        System.out.println(articles.size());
//        System.out.println(firstLentDivs);
        System.out.println(new String(new char[100]).replace('\0', '!'));
    }

    @Test
    public void shouldParseBessarabia() throws IOException {
        String url = NewsTbotApplication.RESOURCES.get(1);
        Document document = Jsoup.connect(url).get();
        final Elements lenta = document.select("div.latestDate");

        final Element todayLenta = lenta.first();
        final List<Article> articles = getArticlesPerDay(todayLenta);
        final Element yesterdayLenta = lenta.get(1);
        articles.addAll(getArticlesPerDay(yesterdayLenta));

        System.out.println(new String(new char[100]).replace('\0', '!'));
        System.out.println(articles.size());
        System.out.println(new String(new char[100]).replace('\0', '!'));
    }

    @Test
    public void shouldSendPostToTest() throws IOException {
        /*InstagramClient instagram = new InstagramClient();
        final Instagram4j instagram4j = instagram.initInstagram(instaUsername, instaPassword);
        instagram.loginInstagram(instagram4j);
        instagram.broadcastMessage()*/
    }

    private List<Article> getArticlesPerDay(Element lenta) {
        final Element lentaDateElement = lenta.select("div.latestDateTitle").first();
        final Element lentaNewsElement = lenta.select("div.latestDatePosts").first();
        final LocalDate lentaLocalDate = LocalDate.parse(lentaDateElement.text(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return lentaNewsElement.select("a")
                .stream()
                .map(element -> articleFromElement(lentaLocalDate, element))
                .collect(Collectors.toList());
    }

    private Article articleFromElement(LocalDate todayLentaLocalDate, Element element) {
        final Article article = new Article();
        article.setLink(element.attr("href"));
        article.setTitle(element.attr("title"));
        final String spanWithArticleTime = element.select("span").first().text();
        final LocalTime articleLocalDateTime = LocalTime.parse(spanWithArticleTime, DateTimeFormatter.ofPattern("HH:mm"));
        article.setDate(LocalDateTime.of(todayLentaLocalDate, articleLocalDateTime));
        return article;
    }
}
