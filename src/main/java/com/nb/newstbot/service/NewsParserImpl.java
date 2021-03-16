package com.nb.newstbot.service;

import com.nb.newstbot.NewsTbotApplication;
import com.nb.newstbot.domain.Article;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.nb.newstbot.NewsTbotApplication.RESOURCES;

/**
 * Implementation for {@link NewsParser}.
 *
 * @author Nick Barban.
 */
@Service
@Slf4j
public class NewsParserImpl implements NewsParser {

    private LocalDate today = LocalDate.now();

    @Override
    public List<Article> getNews() {
        log.info("Fetch all news");
        final List<Article> articles = new ArrayList<>();
        articles.addAll(getBessarabiainformArticles());
        articles.addAll(getBessarabia());
        articles.addAll(getGorod24());
        articles.addAll(getUkrinform());
        return articles.stream().distinct().sorted(Comparator.comparing(Article::getDate)).collect(Collectors.toList());
    }

    @Override
    public List<Article> getLatestNews(Article latest) {
        log.info("Fetch news after {}", latest);
        // TODO by nbarban: 02/03/21 Should be added check that more recent news are available
        final List<Article> news = getNews();
        int latestArticleIndex = news.stream()
                .filter(a -> a.getTitle().equalsIgnoreCase(latest.getTitle()))
                .findFirst()
                .map(news::indexOf)
                .orElse(0);
        news.subList(latestArticleIndex, news.size());
        return news
                .stream()
                .filter(article -> article.getDate().isAfter(latest.getDate()))
                .collect(Collectors.toList());
    }

    private List<Article> getUkrinform() {
        return Collections.emptyList();
    }

    private List<Article> getGorod24() {
        return Collections.emptyList();
    }

    private List<Article> getBessarabia() {
        String url = NewsTbotApplication.RESOURCES.get(1);

        try {
            final Document document = Jsoup.connect(url).get();
            final Elements lenta = document.select("div.latestDate");
            final Element todayLenta = lenta.first();
            final List<Article> articles = getArticlesPerDay(todayLenta);
            final Element yesterdayLenta = lenta.get(1);
            articles.addAll(getArticlesPerDay(yesterdayLenta));

            if (CollectionUtils.isEmpty(articles)) {
                log.error("There are no articles");
            } else {
                log.info("Parsed {} articles from bessarabia", articles.size());
            }

            return articles;
        } catch (IOException e) {
            log.error("Could not connect to url: %s".formatted(url), e);
            return Collections.emptyList();
        }
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

    private List<Article> getBessarabiainformArticles() {
        final String url = RESOURCES.get(0);
        final List<Article> articles = new ArrayList<>();

        try {
            Document document = Jsoup.connect(url).get();
            final Elements lenta = document.select("div.lenta_holder");
            final Element firstLenta = lenta.first();
            final Elements firstLentDivs = firstLenta.select("div");

            LocalDate yesterday = null;
            for (Element e : firstLentDivs) {
                if (e.select("dfn") != null && e.select("dfn").first() != null) {
                    final Article article = new Article();
                    article.setLink(e.select("a").first().attr("href"));
                    article.setTitle(e.select("a").first().text());
                    log.info(e.select("span.fa").text());

                    if (yesterday == null) {
                        article.setDate(LocalDateTime.of(today, LocalTime.parse(e.select("dfn").first().text())));
                    } else {
                        article.setDate(LocalDateTime.of(yesterday, LocalTime.parse(e.select("dfn").first().text())));
                    }

                    articles.add(article);
                } else {
                    log.info(e.toString());
                    final Elements dateDiv = e.select("div.tabview-main-date");
                    if (dateDiv != null) {
                        final String date = dateDiv.text().trim();

                        if (!StringUtils.isEmpty(date)) {
                            try {
                                yesterday = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                                log.info("New start date is: {}", yesterday);
                            } catch (Exception ex) {
                                log.error("Could not parse date text for div: %s".formatted(date), ex);
                            }
                        }
                    }
                }
            }

            if (CollectionUtils.isEmpty(articles)) {
                log.error("There are no articles");
            } else {
                log.info("Parsed {} articles from bessarabia inform", articles.size());
            }

            return articles;
        } catch (IOException e) {
            log.error("Could not connect to url: %s".formatted(url), e);
            return Collections.emptyList();
        }

    }
}
