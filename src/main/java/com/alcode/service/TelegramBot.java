package com.alcode.service;

import com.alcode.config.BotConfig;
import com.alcode.user.Role;
import com.alcode.user.UsersService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final UsersService usersService;

    public TelegramBot(BotConfig config, UsersService usersService) {
        this.config = config;
        this.usersService = usersService;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Boshlash"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error during setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().getChat().getType().equals("supergroup")) {
                // DO NOTHING CHANNEL CHAT ID IS -1001764816733
                return;
            } else {
                Role role = usersService.getRoleByChatId(chatId);

                if (update.hasMessage() && update.getMessage().hasText()) {
                    String messageText = update.getMessage().getText();

                    if (messageText.startsWith("/")) {
                        if (messageText.startsWith("/login ")) {
                            String password = messageText.substring(7);

                            if (password.equals("Xp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmYq3t6w9z$C&F)J@NcRfUjXn2r4u7x!A%D*G-Ka")) {
                                usersService.changeRole(chatId, Role.ROLE_ADMIN);
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            return;
                        }

                        switch (messageText) {
                            case "/start" -> {
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            case "/help" -> {
                                helpCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                                return;
                            }
                            default -> {
                                sendMessage(chatId, "Sorry, command was not recognized");
                                return;
                            }
                        }
                    }

                    if (role.equals(Role.ROLE_ADMIN)) {}
                    else if (role.equals(Role.ROLE_USER)) {
                        sendMessage(chatId, "Let me see...");

                        try {
                            String videoUrl = getVideoLinkFromSite(messageText);
                            log.info("{} is downloaded ", videoUrl);
                            sendMedia(chatId, videoUrl);
                            sendMessage(chatId, "Video downloaded successfully!");
                        } catch (Exception e) {
                            sendMessage(chatId, "An error occurred while downloading the video");
                        }
                    }
                }
            }

        }
    }

    private void startCommandReceived(long chatId, String firstName, String lastName) {
        Role role = usersService.createUser(chatId, firstName, lastName).getRole();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableHtml(true);

        if (role.equals(Role.ROLE_USER)) {
            message.setText("Welcome User, What's up?");
        } else if (role.equals(Role.ROLE_ADMIN)) {
            message.setText("Welcome Admin, What's up?");
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error in startCommandReceived()");
        }
    }

    private void helpCommandReceived(long chatId, String firstName) {
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private String getVideoLinkFromSite(String site) throws IOException {
        URL url = new URL(site);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String html = "";
        String line = in.readLine();
        while (line != null) {
            html += line;
            line = in.readLine();
        }
        in.close();

        String jsonContent = "";
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("script").attr("data-test-id", "leaf-snippet");
        for (Element element : elements) {
            if (element.html().contains(".mp4")) {
                jsonContent = element.html();
                break;
            }
        }

        if (!jsonContent.contains("contentUrl")) {
            Elements videos = doc.select("script#__PWS_DATA__");

            for (Element element : videos) {
                if (element.html().contains(".mp4")) {
                    jsonContent = element.html();
                    break;
                }
            }

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);


            JsonObject props = jsonObject.getAsJsonObject("props");
            JsonObject initialReduxState = props.getAsJsonObject("initialReduxState");
            JsonObject pins = initialReduxState.getAsJsonObject("pins");
            String id = pins.entrySet().iterator().next().getKey();
            JsonObject pin = pins.getAsJsonObject(id);
            JsonObject storyPinData = pin.getAsJsonObject("story_pin_data");
            JsonObject page = storyPinData.getAsJsonArray("pages").get(0).getAsJsonObject();
            JsonObject block = page.getAsJsonArray("blocks").get(0).getAsJsonObject();
            JsonObject video = block.getAsJsonObject("video");
            JsonObject video_list = video.getAsJsonObject("video_list");
            JsonObject V_EXP7 = video_list.getAsJsonObject("V_EXP7");
            System.out.println(V_EXP7);
            String targetUrl = V_EXP7.get("url").getAsString();
            return targetUrl;

        }

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);

        String targetUrl = jsonObject.get("contentUrl").getAsString();
        System.out.println(targetUrl);
        return targetUrl;
    }
    private void sendMedia(Long chatId, String videoUrl) {
        SendVideo video = new SendVideo();
        video.setChatId(chatId);
        video.setVideo(new InputFile(videoUrl));

        try {
            execute(video);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "Video does not exists anymore");
        }
    }

}