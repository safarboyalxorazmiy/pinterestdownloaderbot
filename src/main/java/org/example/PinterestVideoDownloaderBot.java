package org.example;

import com.google.gson.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.example.Main.logger;

public class PinterestVideoDownloaderBot extends TelegramLongPollingBot {
    // 5881206648:AAHenXUMn8Gt_lZeW_IAsym8JP6wfP4jEcc, FreePinterestDownloaderBot

    public static Long users;

    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String txt = msg.getText();
            if (txt.startsWith("/")) {
                switch (txt) {
                    case "/start" -> {
                        sendMsg(msg, "Send me just a link and I will send you a video");
                    }
                    case "/login" -> {

                    }
                    case "/logout" -> {

                    }
                    default -> {
                        sendMsg(msg, "Incorrect command. Please try again");
                    }
                }
            } else if (txt.startsWith("https://www.pinterest.com/")) {
                sendMsg(msg, "Let me see...");


                try {
                    String videoUrl = getVideoLinkFromSite(txt);
                    logger.info("{} is downloaded ", videoUrl);
                    sendMedia(msg, videoUrl);
                    sendMsg(msg, "Video downloaded successfully!");
                } catch (Exception e) {
                    sendMsg(msg, "An error occurred while downloading the video");
                }
            } else {
                sendMsg(msg, "The page you were on trying to send you to an invalid URL");
            }
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

    private void sendMsg(Message msg, String text) {
        SendMessage s = new SendMessage();
        s.setChatId(msg.getChatId());
        s.setText(text);
        try {
            execute(s);
        } catch (TelegramApiException e) {
            logger.warn("Something went wrong during sending a message");
        }
    }

    private void sendMedia(Message msg, String videoUrl) {
        SendVideo video = new SendVideo();
        video.setChatId(msg.getChatId());
        video.setVideo(new InputFile(videoUrl));

        try {
            execute(video);
        } catch (TelegramApiException e) {
            sendMsg(msg, "Video does not exists anymore");
        }
    }

    @Override
    public String getBotUsername() {
        return "FreePinterestDownloaderBot";
    }

    @Override
    public String getBotToken() {
        return "5881206648:AAHenXUMn8Gt_lZeW_IAsym8JP6wfP4jEcc";
    }
}