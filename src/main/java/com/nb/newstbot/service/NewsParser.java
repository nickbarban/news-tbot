package com.nb.newstbot.service;

import com.nb.newstbot.domain.Article;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nick Barban.
 */
public interface NewsParser {

    List<Article> getNews() throws IOException;

    List<Article> getLatestNews(LocalDateTime latest);
}
