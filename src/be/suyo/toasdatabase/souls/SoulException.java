package be.suyo.toasdatabase.souls;

public class SoulException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SoulException() {
        super();
    }

    public SoulException(String s) {
        super(s);
    }

    public SoulException(Throwable t) {
        super(t);
    }

    public SoulException(String s, Throwable t) {
        super(s, t);
    }
}
