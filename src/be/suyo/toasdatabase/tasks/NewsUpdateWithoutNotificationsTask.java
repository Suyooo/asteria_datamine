package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.news.DatabaseStoreCallback;
import be.suyo.toasdatabase.news.NewsManager;

@Task(trigger = Task.Trigger.MANUAL, name = "Update News (Without Notifications)")
public class NewsUpdateWithoutNotificationsTask extends AbstractTask {
    public void run() {
        NewsManager.update(new DatabaseStoreCallback());
    }
}
