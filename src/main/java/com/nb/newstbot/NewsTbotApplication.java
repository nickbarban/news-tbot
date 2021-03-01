package com.nb.newstbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.TimerTask;

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
