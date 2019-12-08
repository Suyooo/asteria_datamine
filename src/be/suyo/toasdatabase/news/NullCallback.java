package be.suyo.toasdatabase.news;

public class NullCallback extends NewsUpdateCallback {
    protected void doOnNewPost(NewsPost post) {
    }

    protected void doOnUpdatedPost(NewsPost post) {
    }

    protected void doOnSubpage(NewsPost subpage, NewsPost parent) {
    }

    protected void doOnFinished() {
    }
}
