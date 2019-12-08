package be.suyo.toasdatabase.logging;

public class Logger {
    public static LoggerIFace currentLogger = new ConsoleLogger();

    public static void print(String s) {
        if (currentLogger != null) {
            currentLogger.doPrint(s);
        }
    }
    
    public static void print(boolean b) {
        if (currentLogger != null) {
            currentLogger.doPrint("" + b);
        }
    }
    
    public static void print(long i) {
        if (currentLogger != null) {
            currentLogger.doPrint("" + i);
        }
    }

    public static void print(Object o) {
        if (currentLogger != null) {
            currentLogger.doPrint(o.toString());
        }
    }

    public static void println(String s) {
        if (currentLogger != null) {
            currentLogger.doPrintln(s);
        }
    }
    
    public static void println(boolean b) {
        if (currentLogger != null) {
            currentLogger.doPrintln("" + b);
        }
    }

    public static void println(long i) {
        if (currentLogger != null) {
            currentLogger.doPrintln("" + i);
        }
    }

    public static void println(Object o) {
        if (currentLogger != null) {
            currentLogger.doPrintln(o.toString());
        }
    }

    public static void notify(String s) {
        if (currentLogger != null) {
            currentLogger.doNotify(s);
        }
    }
}
