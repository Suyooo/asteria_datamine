package be.suyo.toasdatabase.news;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.*;

import com.j256.ormlite.dao.CloseableIterator;

import be.suyo.toasdatabase.utils.Global;
import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.tasks.ShutdownManager;
import be.suyo.toasdatabase.utils.DownloadUtils;

public class NewsManager {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equals("reparse")) {
                if (args.length == 1) {
                    reparseAllContentFiles();
                } else {
                    NewsPost.getLatestByPageId(Integer.parseInt(args[1])).parseContent();
                }
            } else if (args.length == 2 && args[0].equals("redownload")) {
                Set<Long> downloadedSet = new HashSet<>();
                downloadedSet.add(Long.parseLong(args[1]));
                downloadSubpages(NewsPost.getLatestByPageId(Long.parseLong(args[1])).downloadAndParseContent(),
                        downloadedSet, new DatabaseStoreCallback());
            } else {
                update(new DatabaseStoreCallback(new DiscordWebhookCallback()));
                //update(new NullCallback());
            }
        } finally {
            ShutdownManager.shutdown();
        }
    }

    public static void update(NewsUpdateCallback cb) {
        Map<String, String> params = new HashMap<>();
        Set<NewsPost.SubpageRequest> subpages = new HashSet<>();
        Set<Long> downloadedInThisUpdate = new HashSet<>();
        int newPosts = 0;
        int category = 0;
        while (category < 3) {
            params.put("category", "" + (++category));
            int page = 0;
            boolean firstPost = true;
            page: while (true) {
                params.put("p", "" + (++page));
                JSONObject news = DownloadUtils.downloadAndDecryptJsonFromUrl("announce/search_announces", params);
                if (!news.has("list")) {
                    break;
                }
                JSONArray arr = news.getJSONArray("list");
                if (arr.length() <= 0) {
                    break;
                }
                for (int i = 0; i < arr.length(); i++) {
                    NewsPost np = new NewsPost(arr.getJSONObject(i), category);
                    Logger.println("Category " + category + " Post " + ((page - 1) * 50 + i + 1));
                    downloadedInThisUpdate.add(np.postPageId);
                    if (np.isNewPost()) {
                        subpages.addAll(np.downloadAndParseContent());
                        newPosts++;
                        cb.onNewPost(np);
                    } else if (np.isUpdateOfPreviousPost()) {
                        subpages.addAll(np.downloadAndParseContent());
                        newPosts++;
                        cb.onUpdatedPost(np);
                    } else {
                        // still check second post in case the first post is old - might be a stickied one
                        if (!firstPost) break page;
                    }
                    firstPost = false;
                    if (newPosts > 50) {
                        new Error("Emergency canceling news download: over 50 posts").printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }

        downloadSubpages(subpages, downloadedInThisUpdate, cb);
        cb.doOnFinished();
    }

    public static void downloadSubpages(Set<NewsPost.SubpageRequest> subpages, Set<Long> downloadedInThisUpdate,
                                        NewsUpdateCallback cb) {
        Set<NewsPost.SubpageRequest> subsubpages = new HashSet<>();
        while (!subpages.isEmpty()) {
            for (NewsPost.SubpageRequest spReq : subpages) {
                if (spReq instanceof NewsPost.SubpageAnnounceRequest) {
                    NewsPost.SubpageAnnounceRequest req = (NewsPost.SubpageAnnounceRequest) spReq;
                    if (!downloadedInThisUpdate.contains(req.subpageId)) {
                        NewsPost previous = NewsPost.getLatestByPageId(req.subpageId);
                        if (previous != null && previous.postSubpageParent != req.parent.postPageId) {
                            continue;
                        }
                        Logger.println("Downloading subpage " + req.subpageId);
                        NewsPost subpage = new NewsPost(req.parent, req.subpageId);
                        downloadedInThisUpdate.add(subpage.postPageId);
                        subsubpages.addAll(subpage.downloadAndParseContent());
                        cb.onSubpage(subpage, req.parent);
                    }
                } else {
                    NewsPost.SubpageActionRequest req = (NewsPost.SubpageActionRequest) spReq;
                    long newPostId = Global.makeActionURLHash(req.transferPageId, req.params);
                    if (!downloadedInThisUpdate.contains(newPostId)) {
                        NewsPost previous = NewsPost.getLatestByPageId(newPostId);
                        if (previous != null && previous.postSubpageParent != req.parent.postPageId) {
                            continue;
                        }
                        Logger.println("Downloading subpage " + newPostId);
                        NewsPost subpage = new NewsPost(req.parent, req.transferPageId, req.params);
                        downloadedInThisUpdate.add(subpage.postPageId);
                        subsubpages.addAll(subpage.downloadAndParseContent());
                        cb.onSubpage(subpage, req.parent);
                    }
                }
            }
            subpages = subsubpages;
            subsubpages = new HashSet<>();
        }
    }

    public static void reparseAllContentFiles() {
        CloseableIterator<NewsPost> posts = NewsPost.getIterator();
        try {
            while (posts.hasNext()) {
                NewsPost p = posts.next();
                Logger.println("Reparsing " + p.getLinkUrl());
                p.parseContent();
            }
        } finally {
            try {
                posts.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
