package be.suyo.toasdatabase.news;

public class DatabaseStoreCallback extends NewsUpdateCallback {
    public DatabaseStoreCallback() {
        super();
    }

    public DatabaseStoreCallback(NewsUpdateCallback chainedCallback) {
        super(chainedCallback);
    }

    protected void doOnNewPost(NewsPost post) {
        NewsPost.save(post);
    }

    protected void doOnUpdatedPost(NewsPost post) {
        NewsPost.save(post);
    }

    protected void doOnSubpage(NewsPost subpage, NewsPost parent) {
        NewsPost.save(subpage);
    }

    protected void doOnFinished() {
    }
}
