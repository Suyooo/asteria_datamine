package be.suyo.toasdatabase.utils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

public class Multithread {
    private static ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void shutdown() {
        executor.shutdown();
    }

    public static Future<JSONObject> downloadAndDecryptJsonFromUrlAsync(String url, Map<String, String> params) {
        return executor.submit(() -> DownloadUtils.downloadAndDecryptJsonFromUrl(url, params));
    }

    public static Future<String> downloadTextFromUrlAsync(String url) {
        return executor.submit(() -> DownloadUtils.downloadTextFromUrl(url));
    }

    public static Future<byte[]> downloadFileFromResourceUrlAsync(String url, boolean writeToFile) {
        return executor.submit(() -> DownloadUtils.downloadFileFromResourceUrl(url, writeToFile));
    }

    public static Future<byte[]> downloadAndDecryptFileFromResourceUrlAsync(String url, boolean writeToFile) {
        return executor.submit(() -> DownloadUtils.downloadAndDecryptFileFromResourceUrl(url, writeToFile));
    }

    public static <T> void run(Callable<T> call) {
        executor.submit(call);
    }

    public static void run(Runnable call) {
        executor.submit(call);
    }
}
