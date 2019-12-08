package be.suyo.toasdatabase.logging;

public class ConsoleLoggerNoMail extends ConsoleLogger {
    @Override
    public void doNotify(String s) {
        doPrintln("[!!!] " + s);
    }
}
