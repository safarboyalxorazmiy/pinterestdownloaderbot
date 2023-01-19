package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PinterestVideoDownloaderBot extends TelegramLongPollingBot {
    // 5881206648:AAHenXUMn8Gt_lZeW_IAsym8JP6wfP4jEcc, FreePinterestDownloaderBot

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String txt = msg.getText();
            if (txt.startsWith("https://www.pinterest.com/")) {
                sendMsg(msg, "Let me see...");
                try {
                    String videoUrl = getVideoLinkFromSite(txt);
                    sendMedia(msg, videoUrl);
                    sendMsg(msg, "Video downloaded successfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMsg(msg, "An error occurred while downloading the video");
                }
            }
        }
    }

    private String getVideoLinkFromSite(String site) throws IOException {
        URL url = new URL(site);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String html = "";
        String line = in.readLine();
        while (line != null) {
            html = line;
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

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);

        String targetUrl = jsonObject.get("contentUrl").getAsString();
        return targetUrl;
    }

    private void sendMsg(Message msg, String text) {
        SendMessage s = new SendMessage();
        s.setChatId(msg.getChatId());
        s.setText(text);
        try {
            execute(s);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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