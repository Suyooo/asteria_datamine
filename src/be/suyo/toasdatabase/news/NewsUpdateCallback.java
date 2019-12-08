package be.suyo.toasdatabase.news;

public abstract class NewsUpdateCallback {
    NewsUpdateCallback chainedCallback;

    public NewsUpdateCallback() {
        this.chainedCallback = null;
    }

    public NewsUpdateCallback(NewsUpdateCallback chainedCallback) {
        this.chainedCallback = chainedCallback;
    }

    public void onNewPost(NewsPost post) {
        doOnNewPost(post);
        if (chainedCallback != null)
            chainedCallback.onNewPost(post);
    }

    public void onUpdatedPost(NewsPost post) {
        doOnUpdatedPost(post);
        if (chainedCallback != null)
            chainedCallback.onUpdatedPost(post);
    }

    public void onSubpage(NewsPost subpage, NewsPost parent) {
        doOnSubpage(subpage, parent);
        if (chainedCallback != null)
            chainedCallback.onSubpage(subpage, parent);
    }

    public void onFinished() {
        doOnFinished();
        if (chainedCallback != null)
            chainedCallback.onFinished();
    }

    protected abstract void doOnNewPost(NewsPost post);

    protected abstract void doOnUpdatedPost(NewsPost post);

    protected abstract void doOnSubpage(NewsPost subpage, NewsPost parent);

    @SuppressWarnings("EmptyMethod")
    protected abstract void doOnFinished();
}
