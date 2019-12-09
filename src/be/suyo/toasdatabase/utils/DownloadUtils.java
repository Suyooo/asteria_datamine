package be.suyo.toasdatabase.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import be.suyo.toascrypt.file.FileCrypt;
import be.suyo.toascrypt.network.NetworkCrypt;

public class DownloadUtils {
    private static byte MAX_RETRIES = 3;

    private static String readAllToString(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder string = new StringBuilder();
        int currentByte = reader.read();
        while (currentByte != -1) {
            string.append((char) currentByte);
            currentByte = reader.read();
        }
        return string.toString();
    }

    private static byte[] readAllToBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead = input.read(data, 0, data.length);
        while (bytesRead != -1) {
            buffer.write(data, 0, bytesRead);
            bytesRead = input.read(data, 0, data.length);
        }
        return buffer.toByteArray();
    }

    private static InputStream makeUrlInputStream(String urlString, Map<String, String> params) throws IOException {
        URL url = new URL("https://prod.game11.klabgames.net/main_android.php/" + urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        StringBuilder out = new StringBuilder("AssetLastID=" + Global.getValue("current_asset_id") + "&version=" +
                Global.getValue("current_version"));
        for (Map.Entry<String, String> param : params.entrySet()) {
            out.append("&").append(URLEncoder.encode(param.getKey(), "UTF-8")).append("=")
                    .append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }
        byte[] outBytes = out.toString().getBytes();

        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(outBytes.length);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        conn.setRequestProperty("User-Agent", "Asteria Database/Linux/0/0/1");

        conn.connect();
        OutputStream os = conn.getOutputStream();
        os.write(outBytes);
        os.close();
        return conn.getInputStream();
    }

    public static JSONObject downloadAndDecryptJsonFromUrl(String url, Map<String, String> params) {
        byte retries = 0;
        Exception lastEx = null;
        while (retries < MAX_RETRIES) {
            try {
                InputStream input = makeUrlInputStream(url, params);
                String jsonString = new String(NetworkCrypt.decrypt(readAllToString(input).getBytes()));
                return new JSONObject(jsonString);
            } catch (FileNotFoundException e) {
                throw new DownloadException("URL does not exist", e);
            } catch (Exception e) {
                lastEx = e;
                retries++;
            }
        }
        throw new DownloadException("Max retries exceeded, last exception attached", lastEx);
    }

    public static String downloadTextFromUrl(String url) {
        byte retries = 0;
        Exception lastEx = null;
        while (retries < MAX_RETRIES) {
            try {
                InputStream input = new URL("https://prod.game11.klabgames.net/main_android.php/" + url).openStream();
                return readAllToString(input);
            } catch (FileNotFoundException e) {
                throw new DownloadException("URL does not exist", e);
            } catch (Exception e) {
                lastEx = e;
                retries++;
            }
        }
        throw new DownloadException("Max retries exceeded, last exception attached", lastEx);
    }

    public static void downloadUpdatePackage(int package_id, String url) {
        byte retries = 0;
        Exception lastEx = null;
        while (retries < MAX_RETRIES) {
            try {
                InputStream input = new URL(url).openStream();
                byte[] bytes = readAllToBytes(input);
                File file = new File("packages/" + package_id + ".zip");
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new IOException("mkdirs failed");
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                }
                return;
            } catch (FileNotFoundException e) {
                throw new DownloadException("URL does not exist", e);
            } catch (Exception e) {
                lastEx = e;
                retries++;
            }
        }
        throw new DownloadException("Max retries exceeded, last exception attached", lastEx);
    }

    public static byte[] downloadFileFromResourceUrl(String url, boolean writeToFile) {
        return downloadFileFromResourceUrl(url, writeToFile, "assets");
    }

    public static byte[] downloadFileFromResourceUrl(String url, boolean writeToFile, String destFolder) {
        byte retries = 0;
        Exception lastEx = null;
        while (retries < MAX_RETRIES) {
            try {
                InputStream input = new URL("https://prod-resource.game11.klabgames.net/" + url).openStream();
                byte[] bytes = readAllToBytes(input);
                if (writeToFile) {
                    File file = new File(destFolder + "/" + url);
                    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                        throw new IOException("mkdirs failed: " + file.getParentFile());
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(bytes);
                    }
                }
                return bytes;
            } catch (FileNotFoundException e) {
                throw new DownloadException("URL does not exist", e);
            } catch (Exception e) {
                lastEx = e;
                retries++;
            }
        }
        throw new DownloadException("Max retries exceeded, last exception attached", lastEx);
    }

    public static byte[] downloadAndDecryptFileFromResourceUrl(String url, boolean writeToFile) {
        return downloadAndDecryptFileFromResourceUrl(url, writeToFile, "assets");
    }

    public static byte[] downloadAndDecryptFileFromResourceUrl(String url, boolean writeToFile, String filename) {
        byte retries = 0;
        Exception lastEx = null;
        while (retries < MAX_RETRIES) {
            try {
                InputStream input = new URL("https://prod-resource.game11.klabgames.net/" + url).openStream();
                byte[] bytes = readAllToBytes(input);
                bytes = FileCrypt.decrypt(bytes, url);
                if (writeToFile) {
                    File file = new File(filename + "/" + url);
                    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                        throw new IOException("mkdirs failed");
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(bytes);
                    }
                }
                return bytes;
            } catch (FileNotFoundException e) {
                throw new DownloadException("URL does not exist", e);
            } catch (Exception e) {
                lastEx = e;
                retries++;
            }
        }
        throw new DownloadException("Max retries exceeded, last exception attached", lastEx);
    }
}
