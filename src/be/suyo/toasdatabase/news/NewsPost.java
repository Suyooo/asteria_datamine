package be.suyo.toasdatabase.news;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.suyo.toasdatabase.gui.globals.GlobalValuesWindow;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.Global;
import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;
import be.suyo.toasdatabase.utils.DownloadException;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Multithread;

@DatabaseTable(tableName = "news")
public class NewsPost {
    private static Dao<NewsPost, Long> dao = DatabaseConnection.getDao(NewsPost.class);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("M/d H:mm");
    private static SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyMMdd");

    private static PreparedQuery<NewsPost> queryByPageId;
    private static SelectArg queryByPageIdArg;

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        originalDateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));

        try {
            QueryBuilder<NewsPost, Long> queryBuilder = dao.queryBuilder();
            queryByPageIdArg = new SelectArg();
            queryBuilder.where().eq("news_page_id", queryByPageIdArg);
            queryBuilder.orderBy("news_version", false);
            queryByPageId = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static NewsPost getLatestByPageId(long id) {
        try {
            queryByPageIdArg.setValue(id);
            return dao.queryForFirst(queryByPageId);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(NewsPost np) {
        try {
            dao.createOrUpdate(np);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<NewsPost> getIterator() {
        return dao.iterator();
    }

    @DatabaseField(columnName = "news_id", generatedId = true)
    public int postId;

    @DatabaseField(columnName = "news_page_id", index = true)
    public long postPageId;

    @DatabaseField(columnName = "news_version")
    public int postVersion;

    @DatabaseField(columnName = "news_title")
    public String postTitle;

    @DatabaseField(columnName = "news_category")
    public int postCategory;

    @DatabaseField(columnName = "news_date_original", dataType = DataType.DATE_LONG)
    public Date postOriginallyPostedDate;

    @DatabaseField(columnName = "news_date_from", dataType = DataType.DATE_LONG)
    public Date postDisplayDateFrom;

    @DatabaseField(columnName = "news_date_to", dataType = DataType.DATE_LONG)
    public Date postDisplayDateTo;

    @DatabaseField(columnName = "news_image_or_action")
    public String postImageOrAction;

    public NewsPost previousVersion;

    @DatabaseField(columnName = "news_subpage_parent")
    public long postSubpageParent;

    public NewsPost() {
    }

    @SuppressWarnings("deprecation")
    // Main Constructor
    public NewsPost(JSONObject newsEntryJson, int category) {
        this.postPageId = newsEntryJson.getInt("Id");
        this.postCategory = category;
        this.postTitle = newsEntryJson.getString("Title");
        try {
            Calendar jstTime = Global.getJSTTime();
            this.postDisplayDateFrom = dateFormat.parse(newsEntryJson.getString("DisplayDateFrom"));
            this.postDisplayDateFrom.setYear(jstTime.get(Calendar.YEAR) - 1900);
            this.postDisplayDateTo = dateFormat.parse(newsEntryJson.getString("DisplayDateTo"));
            this.postDisplayDateTo.setYear(jstTime.get(Calendar.YEAR) - 1900);

            // if in the first half of a year, assume that all posts with July to December dates are from last year
            if (jstTime.get(Calendar.MONTH) <= 5) {
                if (this.postDisplayDateFrom.getMonth() >= 6) {
                    this.postDisplayDateFrom.setYear(this.postDisplayDateFrom.getYear() - 1);
                }
                if (this.postDisplayDateTo.getMonth() >= 6) {
                    this.postDisplayDateTo.setYear(this.postDisplayDateTo.getYear() - 1);
                }
            }

            try {
                // attempt to read date the post was written on from the page ID
                int origDatePart = (int) this.postPageId;
                while (origDatePart > 999999) {
                    origDatePart /= 10;
                }
                this.postOriginallyPostedDate = originalDateFormat.parse("" + origDatePart);

                if (this.postOriginallyPostedDate.getYear() < 2014 - 1900 ||
                        this.postOriginallyPostedDate.getYear() > 2025 - 1900) {
                    throw new ParseException("" + origDatePart, 0);
                }
            } catch (ParseException e) {
                // attempt failed - use post date
                this.postOriginallyPostedDate = this.postDisplayDateFrom;
            }
        } catch (ParseException e) {
            System.err.println("Failed to parse date");
            throw new RuntimeException(e);
        }

        this.previousVersion = NewsPost.getLatestByPageId(this.postPageId);
        if (this.previousVersion == null) {
            this.postVersion = 1;
        } else {
            this.postVersion = this.previousVersion.postVersion + 1;
        }
    }

    // Announce Subpage Constructor
    public NewsPost(NewsPost parent, long id) {
        this.postPageId = id;
        this.postCategory = 4;
        this.postTitle = parent.postTitle + " (Subpage)";
        this.postDisplayDateFrom = parent.postDisplayDateFrom;
        this.postDisplayDateTo = parent.postDisplayDateTo;
        this.postOriginallyPostedDate = parent.postOriginallyPostedDate;
        this.postSubpageParent = parent.postPageId;

        this.previousVersion = NewsPost.getLatestByPageId(this.postPageId);
        if (this.previousVersion == null) {
            this.postVersion = 1;
        } else {
            this.postVersion = this.previousVersion.postVersion + 1;
        }
    }

    // Action Subpage Constructor
    public NewsPost(NewsPost parent, String transferPageId, String params) {
        this.postImageOrAction =
                "transfer_page_id=" + URLEncoder.encode(transferPageId, StandardCharsets.UTF_8) + "&params=" +
                        URLEncoder.encode(params, StandardCharsets.UTF_8);
        this.postPageId = Global.makeActionURLHash(transferPageId, params);
        this.postCategory = 5;
        this.postTitle = parent.postTitle + " (Subpage)";
        this.postDisplayDateFrom = parent.postDisplayDateFrom;
        this.postDisplayDateTo = parent.postDisplayDateTo;
        this.postOriginallyPostedDate = parent.postOriginallyPostedDate;
        this.postSubpageParent = parent.postPageId;

        this.previousVersion = NewsPost.getLatestByPageId(this.postPageId);
        if (this.previousVersion == null) {
            this.postVersion = 1;
        } else {
            this.postVersion = this.previousVersion.postVersion + 1;
        }
    }

    public static NewsPost createCloneWithNewVersion(NewsPost other) {
        NewsPost newPost = new NewsPost();
        newPost.postPageId = other.postPageId;
        newPost.postCategory = other.postCategory;
        newPost.postTitle = other.postTitle;
        newPost.postDisplayDateFrom = other.postDisplayDateFrom;
        newPost.postDisplayDateTo = other.postDisplayDateTo;
        newPost.postOriginallyPostedDate = other.postOriginallyPostedDate;
        newPost.postImageOrAction = other.postImageOrAction;
        newPost.postVersion = other.postVersion + 1;
        newPost.previousVersion = other;
        newPost.postSubpageParent = other.postSubpageParent;
        return newPost;
    }

    public Set<SubpageRequest> downloadAndParseContent() {
        String content;
        if (this.postCategory == 4) {
            content = DownloadUtils.downloadTextFromUrl(
                    "webview/index?id=" + this.postPageId + "&version=" + Global.getValue("current_version"));
        } else if (this.postCategory == 5) {
            content = DownloadUtils.downloadTextFromUrl(
                    "transfer/action?" + this.postImageOrAction + "&version=" + Global.getValue("current_version") +
                            "&player_id=" + Global.getValue("player_id"));
        } else {
            content = DownloadUtils.downloadTextFromUrl(
                    "announce/view?id=" + this.postPageId + "&version=" + Global.getValue("current_version"));
        }
        File contentFile = new File(getLinkUrl() + "_orig");
        if (!contentFile.getParentFile().exists() && !contentFile.getParentFile().mkdirs()) {
            throw new RuntimeException("mkdirs failed");
        }

        try (FileOutputStream fos = new FileOutputStream(contentFile)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to write original news post content");
            throw new RuntimeException(e);
        }

        Multithread.run((Callable<Void>) () -> {
            parseContent();
            return null;
        });

        Document doc = Jsoup.parse(content);
        Map<String, Future<byte[]>> cssToCheck = new HashMap<>();
        List<Future<byte[]>> futuresToWaitFor = new ArrayList<>();
        Set<SubpageRequest> subpages = new HashSet<>();

        for (Element a : doc.select("a")) {
            if (a.attr("href").startsWith("localappli://transfer?transfer_page_id=999001&transfer_page_parameter=") ||
                    a.attr("href")
                            .startsWith("localappli://transfer?transfer_page_id=999003&transfer_page_parameter=")) {
                // download subpages if neccessary 
                if (!a.attr("href").substring(70).startsWith("guild")) {
                    long subpageId = Long.parseLong(a.attr("href").substring(70));
                    subpages.add(new SubpageAnnounceRequest(this, subpageId));
                }
            }
        }
        for (Element form : doc.select("form")) {
            if (form.attr("action").equals("/transfer/action")) {
                // download subpages if neccessary
                String tpi = "";
                String p = "";
                for (Element input : form.select("input")) {
                    if (input.attr("name").equals("transfer_page_id")) {
                        tpi = input.attr("value");
                    } else if (input.attr("name").equals("params")) {
                        p = input.attr("value");
                    }
                }
                subpages.add(new SubpageActionRequest(this, tpi, p));
            }
        }
        for (Element link : doc.select("link")) {
            if (!link.attr("rel").equals("stylesheet")) {
                continue;
            }
            List<Future<byte[]>> future = new ArrayList<>();
            String url = downloadResource(link.attr("href"), future);
            if (url.startsWith("/asteria/assets")) {
                cssToCheck.put(url, future.get(0));
            } else {
                futuresToWaitFor.add(future.get(0));
            }
        }
        for (Element img : doc.select("img")) {
            downloadResource(img.attr("src"), futuresToWaitFor);
        }
        for (Element script : doc.select("script")) {
            if (!script.hasAttr("src")) {
                continue;
            }
            downloadResource(script.attr("src"), futuresToWaitFor);
        }

        Pattern pattern = Pattern.compile("url(\\s*\\(\\s*['\"]*\\s*)(.*?)\\s*['\"]*\\s*\\)");
        for (Element style : doc.select("style")) {
            for (DataNode data : style.dataNodes()) {
                String css = data.getWholeData();
                Matcher matcher = pattern.matcher(css);
                while (matcher.find()) {
                    String origUrl = matcher.group(2);
                    downloadResource(origUrl, futuresToWaitFor);
                }
            }
        }
        for (Element el : doc.select("*")) {
            if (!el.hasAttr("style")) {
                continue;
            }
            String css = el.attr("style");
            Matcher matcher = pattern.matcher(css);
            while (matcher.find()) {
                String origUrl = matcher.group(2);
                downloadResource(origUrl, futuresToWaitFor);
            }
        }
        for (Map.Entry<String, Future<byte[]>> cssFuture : cssToCheck.entrySet()) {
            try {
                String css = new String(cssFuture.getValue().get());
                Matcher matcher = pattern.matcher(css);
                while (matcher.find()) {
                    String origUrl = matcher.group(2);
                    String url = downloadResource(origUrl, futuresToWaitFor);
                    css = css.replace(origUrl, url);
                }
                File file = new File(cssFuture.getKey().substring(9));
                if (!file.getParentFile().exists() && file.getParentFile().mkdirs()) {
                    throw new IOException("mkdirs failed");
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(css.getBytes());
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                System.err.println("Failed to parse css");
                throw new RuntimeException(e);
            }
        }

        for (Future<byte[]> future : futuresToWaitFor) {
            try {
                future.get();
            } catch (Exception e) {
                if (e instanceof ExecutionException && e.getCause() != null &&
                        e.getCause() instanceof DownloadException) {
                    if (!(e.getCause().getCause() instanceof FileNotFoundException)) {
                        System.err.println("Download failed");
                        e.getCause().getCause().printStackTrace();
                    }
                } else {
                    System.err.println("Error while multithreading");
                    e.printStackTrace();
                }
            }
        }

        return subpages;
    }

    public void parseContent() {
        File contentFile = new File(getLinkUrl() + "_orig");
        String content;
        try {
            content = new String(Files.readAllBytes(contentFile.toPath()));
        } catch (IOException e) {
            System.err.println("Failed to read original news post content");
            throw new RuntimeException(e);
        }
        // asteria devs cant html lol
        content = content.replaceAll("style=\"([^\"]*?)\"\\s*?style=\"([^\"]*?)\"", "style=\"$1;$2\"");

        Document doc = Jsoup.parse(content);
        if (this.postCategory <= 3) {
            Element headerImg = doc.selectFirst("#wrapper > img");
            if (headerImg != null) {
                this.postImageOrAction = headerImg.attr("src");
                int endIndex = this.postImageOrAction.indexOf('?');
                this.postImageOrAction = this.postImageOrAction
                        .substring(43, (endIndex == -1) ? this.postImageOrAction.length() : endIndex);
            }
        }

        doc.select("input[type=\"button\"]").attr("disabled", "true");

        for (Element a : doc.select("a")) {
            a.removeAttr("onClick");
            if (a.hasAttr("href")) {
                if ((a.attr("href")
                        .startsWith("localappli://transfer?transfer_page_id=999001&transfer_page_parameter=") ||
                        a.attr("href").startsWith(
                                "localappli://transfer?transfer_page_id=999003&transfer_page_parameter=")) &&
                        !a.attr("href").substring(70).startsWith("guild")) {
                    // transfers to another page should work
                    a.attr("href", "/asteria/news/" + a.attr("href").substring(70));
                    a.attr("target", "_parent");
                } else if (a.attr("href")
                        .startsWith("localappli://transfer?transfer_page_id=999002&transfer_page_parameter=")) {
                    // external page links should work
                    a.attr("href", URLDecoder.decode(a.attr("href").substring(70), StandardCharsets.UTF_8));
                    a.attr("target", "_blank");
                } else {
                    a.removeAttr("href");
                }
            }
        }
        for (Element form : doc.select("form")) {
            if (form.attr("action").equals("/transfer/action")) {
                String tpi = "";
                String p = "";
                for (Element input : form.select("input")) {
                    if (input.attr("name").equals("transfer_page_id")) {
                        tpi = input.attr("value");
                    } else if (input.attr("name").equals("params")) {
                        p = input.attr("value");
                    }
                }
                form.attr("action", "/asteria/news/" + Global.makeActionURLHash(tpi, p));
                form.attr("target", "_parent");
                form.select("input[type=\"button\"]").removeAttr("disabled");
            }
        }

        for (Element link : doc.select("link")) {
            if (!link.attr("rel").equals("stylesheet")) {
                continue;
            }
            String url = getResourceUrl(link.attr("href"));
            link.attr("href", url);
        }
        doc.head().append("<link rel=\"stylesheet\" href=\"/asteria/css/newspost.css\">");
        for (Element img : doc.select("img")) {
            img.attr("src", getResourceUrl(img.attr("src")));
        }
        for (Element script : doc.select("script")) {
            if (!script.hasAttr("src")) {
                continue;
            }
            script.attr("src", getResourceUrl(script.attr("src")));
        }

        Pattern pattern = Pattern.compile("url(\\s*\\(\\s*['\"]*\\s*)(.*?)\\s*['\"]*\\s*\\)");
        for (Element style : doc.select("style")) {
            for (DataNode data : style.dataNodes()) {
                String css = data.getWholeData();
                Matcher matcher = pattern.matcher(css);
                while (matcher.find()) {
                    String origUrl = matcher.group(2);
                    css = css.replace(origUrl, getResourceUrl(origUrl));
                }
                data.setWholeData(css);
            }
        }
        for (Element el : doc.select("*")) {
            if (!el.hasAttr("style")) {
                continue;
            }
            String css = el.attr("style");
            Matcher matcher = pattern.matcher(css);
            while (matcher.find()) {
                String origUrl = matcher.group(2);
                css = css.replace(origUrl, getResourceUrl(origUrl));
            }
            el.attr("style", css);
        }

        contentFile = new File(getLinkUrl());
        if (!contentFile.getParentFile().exists() && contentFile.getParentFile().mkdirs()) {
            throw new RuntimeException("mkdirs failed");
        }
        try (FileOutputStream fos = new FileOutputStream(contentFile)) {
            fos.write(doc.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Failed to write news post content");
            throw new RuntimeException(e);
        }
    }

    // Downloads a resource from the game servers if there, returns the URL to replace the original URL with
    private String downloadResource(String origUrl, List<Future<byte[]>> futureList) {
        int startIndex;
        if (origUrl.startsWith("/")) {
            startIndex = 1;
        } else if (origUrl.startsWith("../../")) {
            startIndex = 6;
        } else if (origUrl.startsWith("../")) {
            startIndex = 3;
        } else if (origUrl.startsWith("https://prod.game11.klabgames.net/")) {
            startIndex = 34;
        } else if (origUrl.startsWith("https://prod-resource.game11.klabgames.net/")) {
            startIndex = 43;
        } else {
            return origUrl;
        }

        int endIndex = origUrl.indexOf('?');
        String url = origUrl.substring(startIndex, (endIndex == -1) ? origUrl.length() : endIndex).replace("\\/", "/");
        futureList.add(Multithread.downloadFileFromResourceUrlAsync(url, true));
        return "/asteria/assets/" + url;
    }

    private String getResourceUrl(String origUrl) {
        int startIndex;
        if (origUrl.startsWith("/")) {
            startIndex = 1;
        } else if (origUrl.startsWith("../../")) {
            startIndex = 6;
        } else if (origUrl.startsWith("../")) {
            startIndex = 3;
        } else if (origUrl.startsWith("https://prod.game11.klabgames.net/")) {
            startIndex = 34;
        } else if (origUrl.startsWith("https://prod-resource.game11.klabgames.net/")) {
            startIndex = 43;
        } else {
            return origUrl;
        }

        int endIndex = origUrl.indexOf('?');
        String url = origUrl.substring(startIndex, (endIndex == -1) ? origUrl.length() : endIndex).replace("\\/", "/");
        return "/asteria/assets/" + url;
    }

    public String getLinkUrl() {
        return "assets/news/" + this.postPageId + "-" + this.postVersion + ".html";
    }

    public boolean isNewPost() {
        return this.previousVersion == null;
    }

    public boolean isUpdateOfPreviousPost() {
        if (this.previousVersion == null) {
            return true;
        }
        if (!this.postDisplayDateFrom.after(this.previousVersion.postDisplayDateFrom)) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(this.previousVersion.postDisplayDateFrom);
        c.add(Calendar.YEAR, 1);
        return !c.getTime().equals(this.postDisplayDateFrom);
    }

    public class SubpageRequest {
    }

    public class SubpageAnnounceRequest extends SubpageRequest {
        public final NewsPost parent;
        public final Long subpageId;

        public SubpageAnnounceRequest(NewsPost parent, Long subpageId) {
            this.parent = parent;
            this.subpageId = subpageId;
        }
    }

    public class SubpageActionRequest extends SubpageRequest {
        public final NewsPost parent;
        public final String transferPageId;
        public final String params;

        public SubpageActionRequest(NewsPost parent, String transferPageId, String params) {
            this.parent = parent;
            this.transferPageId = transferPageId;
            this.params = params;
        }
    }
}
