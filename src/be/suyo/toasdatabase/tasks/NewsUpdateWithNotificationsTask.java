package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.news.DatabaseStoreCallback;
import be.suyo.toasdatabase.news.DiscordWebhookCallback;
import be.suyo.toasdatabase.news.NewsManager;

@Task(trigger = Task.Trigger.QUARTER_HOUR, name = "Update News (With Notifications)")
public class NewsUpdateWithNotificationsTask extends AbstractTask {
    public void run() {
        NewsManager.update(new DatabaseStoreCallback(new DiscordWebhookCallback()));
    }
}
