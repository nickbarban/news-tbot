package com.nb.newstbot.service;

import com.nb.newstbot.domain.Article;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        final List<Article> articles = getBessarabiainformArticles();
        articles.addAll(getBessarabia());
        articles.addAll(getGorod24());
        articles.addAll(getUkrinform());
        return articles.stream().sorted(Comparator.comparing(Article::getDate)).collect(Collectors.toList());
    }

    @Override
    public List<Article> getLatestNews(LocalDateTime latest) {
        // TODO by nbarban: 02/03/21 Should be added check that more recent news are available
        return getNews().stream().filter(article -> article.getDate().isAfter(latest)).collect(Collectors.toList());
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

        try {
            Document document = Jsoup.connect(url).get();
            final Elements lenta = document.select("div.lenta_holder");
            final Element firstLenta = lenta.first();
            final Elements firstLentDivs = firstLenta.select("div");
            final List<Article> articles = firstLentDivs.stream()
                    .filter(div -> div.select("dfn") != null && div.select("dfn").first() != null)
                    .map(div -> {
                        Article article = new Article();
                        article.setDate(LocalDateTime.of(LocalDate.now(), LocalTime.parse(div.select("dfn").first().text())));
                        article.setLink(div.select("a").first().attr("href"));
                        article.setTitle(div.select("a").first().text());
                        return article;
                    })
                    .collect(Collectors.toList());

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
