package be.suyo.toasdatabase.tasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Task {
    Trigger trigger();

    String name();

    enum Trigger {
        MANUAL, ON_DAILY_RESET, // 5:00 jst, 20:00 utc
        UPDATE_AVAILABLE, // executed automatically if updated packages need to be downloaded
        QUARTER_HOUR // :00, :15, :30, :45
    }
}