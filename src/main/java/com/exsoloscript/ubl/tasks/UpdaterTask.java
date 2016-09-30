package com.exsoloscript.ubl.tasks;

import com.exsoloscript.ubl.banlist.BanListParser;
import com.exsoloscript.ubl.banlist.BanListRecord;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class UpdaterTask implements Runnable {

    private String banListUrl;
    private int timeoutSeconds;
    private Logger logger;

    private UpdaterTaskCallback callback;

    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .retryOnConnectionFailure(true)
                .readTimeout(this.timeoutSeconds, TimeUnit.SECONDS)
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(this.banListUrl)
                .header("Accept-Language", "en-US,en;q=0.8")
                .header("User-Agent", "Mozilla")
                .header("Referer", "google.com");

        Response response = null;

        this.logger.info("Requesting newest version of the UBL.");

        try {
            response = client.newCall(requestBuilder.build()).execute();
        } catch (IOException e) {
            this.logger.error("Error while requesting the banlist. Did you specify the correct banlist-url?", e);
        }

        try {
            String data = response.body().string();
            this.callback.handle(BanListParser.parseBans(data));
        } catch (Exception e) {
            this.logger.error("Error while reading the received banlist", e);
        }
    }

    public interface UpdaterTaskCallback {
        void handle(Set<BanListRecord> profiles) throws Exception;
    }
}
