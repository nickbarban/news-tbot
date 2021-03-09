package com.nb.newstbot.service;

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
    @Override
    public List<Article> getNews() {
        log.info("Fetch all news");
        final List<Article> articles = getBessarabiainformArticles();
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
        return Collections.emptyList();
    }

    private List<Article> getBessarabiainformArticles() {
        final String url = RESOURCES.get(0);
        final List<Article> articles = new ArrayList<>();

        try {
            Document document = Jsoup.connect(url).get();
            final Elements lenta = document.select("div.lenta_holder");
            final Element firstLenta = lenta.first();
            final Elements firstLentDivs = firstLenta.select("div");

            LocalDate startDate = LocalDate.now();
            for (Element e : firstLentDivs) {
                if (e.select("dfn") != null && e.select("dfn").first() != null) {
                    final Article article = new Article();
                    article.setDate(LocalDateTime.of(startDate, LocalTime.parse(e.select("dfn").first().text())));
                    article.setLink(e.select("a").first().attr("href"));
                    article.setTitle(e.select("a").first().text());
                    articles.add(article);
                } else {
                    log.info(e.toString());
                    final Elements dateDiv = e.select("div.tabview-main-date");
                    if (dateDiv != null) {
                        final String date = dateDiv.text().trim();

                        if (!StringUtils.isEmpty(date)) {
                            try {
                                startDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                                log.info("New start date is: {}", startDate);
                            } catch (Exception ex) {
                                log.error("Could not parse date text for div: %s".formatted(date), ex);
                            }
                        }
                    }
                }
            }

            if (CollectionUtils.isEmpty(articles)) {
                log.error("There are no articles");
            }

            return articles;
        } catch (IOException e) {
            log.error("Could not connect to url: %s".formatted(url), e);
            return Collections.emptyList();
        }

    }
}
