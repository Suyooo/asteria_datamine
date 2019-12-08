package be.suyo.toasdatabase.utils;

public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DatabaseException() {
        super();
    }

    public DatabaseException(String s) {
        super(s);
    }

    public DatabaseException(Throwable t) {
        super(t);
    }

    public DatabaseException(String s, Throwable t) {
        super(s, t);
    }
}
