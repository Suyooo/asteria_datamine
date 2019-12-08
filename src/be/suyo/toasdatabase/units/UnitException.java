package be.suyo.toasdatabase.units;

public class UnitException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnitException() {
        super();
    }

    public UnitException(String s) {
        super(s);
    }

    public UnitException(Throwable t) {
        super(t);
    }

    public UnitException(String s, Throwable t) {
        super(s, t);
    }
}
