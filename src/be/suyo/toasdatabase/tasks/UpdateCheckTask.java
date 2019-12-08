package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.packages.PackageDownloader;

@Task(trigger = Task.Trigger.QUARTER_HOUR,
    name = "Check For New Packages (And Run Associated Tasks)")
public class UpdateCheckTask extends AbstractTask {
    public void run() {
        if (PackageDownloader.isNewPackageAvailable()) {
            Logger.println("New Updates Available");
            for (Class<? extends AbstractTask> task : TaskManager
                .getTasksForTrigger(Task.Trigger.UPDATE_AVAILABLE)) {
                TaskManager.runTaskByClass(task);
            }
        }
    }
}
