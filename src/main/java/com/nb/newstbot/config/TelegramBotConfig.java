package com.nb.newstbot.config;

import com.nb.newstbot.domain.Tbot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

/**
 * @author Nick Barban.
 */
@Slf4j
@Configuration
public class TelegramBotConfig {
    private static final String BOT_NAME = "parse_news_bot";
    private static final String BOT_TOKEN = "1689880233:AAHad53D1fmhc9tsd8cmkdUTV6sWM5klLNc";

    @PostConstruct
    public void start() {
        try {
            log.info("Instantiate Telegram Bots API...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            log.info("Register Telegram Bots API...");
            botsApi.registerBot(new Tbot(BOT_NAME, BOT_TOKEN));
        } catch (TelegramApiException e) {
            log.error("Exception instantiate Telegram Bot!", e);
        }
    }
}