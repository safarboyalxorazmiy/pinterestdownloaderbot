package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main  {
    public static Logger logger = LoggerFactory.getLogger("SampleLogger");

    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new PinterestVideoDownloaderBot());
            logger.info("Common Telegram bot started.");
        } catch (TelegramApiException e) {
            logger.error("Something went wrong during registering new bot..");
        }
    }
}