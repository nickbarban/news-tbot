package com.nb.newstbot.service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Nick Barban.
 */
@Slf4j
public class InstagramClient {
    public void send(String message) {
        log.info("Send to instagram: {}", message);
    }
}
