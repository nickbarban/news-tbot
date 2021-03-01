package com.nb.newstbot;

import com.nb.newstbot.domain.Tbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@SpringBootApplication
public class NewsTbotApplication {

    public static final List<String> RESOURCES = List.of(
            "https://bessarabiainform.com",
            "https://bessarabia.ua",
            "http://gorod24.info",
            "https://www.ukrinform.ru");

    public static void main(String[] args) {
        SpringApplication.run(NewsTbotApplication.class, args);
    }

}
