package be.suyo.toasdatabase.utils;

public class DownloadException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DownloadException() {
        super();
    }

    public DownloadException(String s) {
        super(s);
    }

    public DownloadException(Throwable t) {
        super(t);
    }

    public DownloadException(String s, Throwable t) {
        super(s, t);
    }
}
