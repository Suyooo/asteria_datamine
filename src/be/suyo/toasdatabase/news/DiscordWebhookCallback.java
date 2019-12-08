package be.suyo.toasdatabase.news;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import be.suyo.toasdatabase.tasks.AbstractTask;
import be.suyo.toasdatabase.tasks.ShutdownManager;

public class DiscordWebhookCallback extends NewsUpdateCallback {
    private static String[] categories = {"Campaigns etc.", "Updates", "Errors"};
    private static String webhookUrl =
            "https://discordapp.com/api/webhooks/636747652075225108/AaAc_6ys-nVFZGGJbF6ZZ0sVagCDDJNMrSHJsbQJW_B4BN0QtUK6_mtaM88zAhYrrezY";

    public DiscordWebhookCallback() {
        super();
    }

    public DiscordWebhookCallback(NewsUpdateCallback chainedCallback) {
        super(chainedCallback);
    }

    private static void sendWebhookMessage(String header, String title, Date time, String linkUrl, String imageUrl) {
        try {
            URL url = new URL(webhookUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            TimeZone tz = TimeZone.getTimeZone("Japan");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
            df.setTimeZone(tz);

            String out =
                    "{ \"embeds\": [ { \"title\": \"" + title + "\", \"url\": \"https://suyo.be/asteria/" + linkUrl +
                            "\", \"timestamp\": \"" + df.format(time) +
                            "\", \"footer\": { \"text\": \"Asteria Database\" }, \"image\": { \"url\": \"https://suyo.be/asteria/assets/" +
                            imageUrl + "\" }, \"author\": { \"name\": \"" + header + "\" } } ] }";
            byte[] outBytes = out.getBytes();

            conn.setRequestMethod("POST");
            conn.setDoInput(false);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(outBytes.length);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("User-Agent", "Asteria Database");

            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(outBytes);
            os.close();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // don't care
            }
        } catch (IOException e) {
            System.err.println("Unable to send Webhook notification");
            e.printStackTrace();
        }
    }

    protected void doOnNewPost(NewsPost post) {
        if (post.postCategory > 3) {
            return;
        }
        ShutdownManager.addPostCopyTask(new AbstractTask() {
            public void run() {
                sendWebhookMessage("New News Post in " + categories[post.postCategory - 1], post.postTitle,
                        post.postDisplayDateFrom, "news/" + post.postPageId + "/" + post.postVersion,
                        post.postImageOrAction);
            }
        });
    }

    protected void doOnUpdatedPost(NewsPost post) {
        if (post.postCategory > 3) {
            return;
        }
        ShutdownManager.addPostCopyTask(new AbstractTask() {
            public void run() {
                sendWebhookMessage("Updated News Post in " + categories[post.postCategory - 1], post.postTitle,
                        post.postDisplayDateFrom, "news/" + post.postPageId + "/" + post.postVersion,
                        post.postImageOrAction);
            }
        });
    }

    protected void doOnSubpage(NewsPost subpage, NewsPost parent) {
    }

    protected void doOnFinished() {
    }
}
