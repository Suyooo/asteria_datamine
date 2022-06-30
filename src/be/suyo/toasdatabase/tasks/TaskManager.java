package be.suyo.toasdatabase.tasks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Global;
import org.json.JSONArray;
import org.json.JSONObject;

public class TaskManager {
    @SuppressWarnings("rawtypes")
    private static Class[] allTasks =
            {UpdateCheckTask.class, CheckPendingUnitsTask.class, PartnerScheduleUpdateTask.class,
                    NewsUpdateWithNotificationsTask.class, NewsUpdateWithoutNotificationsTask.class,
                    DownloadNewPackagesTask.class};

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        JSONObject adddata = DownloadUtils.downloadAndDecryptJsonFromUrl("api/getAdditionalDataList", new HashMap<>());
        if (!adddata.has("additional_data_list")) {
            // maintenance
            return;
        }
        if (args.length >= 2 && args[0].equals("run")) {
            for (int a = 1; a < args.length; a++) {
                try {
                    Class<? extends AbstractTask> c =
                            (Class<? extends AbstractTask>) Class.forName("be.suyo.toasdatabase.tasks." + args[a]);
                    runTaskByClass(c);
                } catch (ClassNotFoundException e) {
                    System.err.println("Task called " + args[a] + " does not exist.");
                }
            }
        } else if (args.length >= 2 && args[0].equals("trigger")) {
            for (int a = 1; a < args.length; a++) {
                if (args[a].equals("MANUAL")) {
                    continue;
                }
                for (Class<? extends AbstractTask> task : getTasksForTrigger(Task.Trigger.valueOf(args[a]))) {
                    runTaskByClass(task);
                }
            }
        } else {
            System.err.println("Usage: TaskManager [run|trigger] ...");
            System.err.println("  run: Run the task(s) given");
            System.err.println("    Available Tasks:");
            for (Class<? extends AbstractTask> c : allTasks) {
                System.err.println("      " + c.getSimpleName());
            }
            System.err.println("  trigger: Run tasks associated with the triggers given");
            System.err.println("    Available Triggers:");
            for (Task.Trigger t : Task.Trigger.values()) {
                if (t == Task.Trigger.MANUAL) {
                    continue;
                }
                System.err.println("      " + t.name());
            }
        }
        ShutdownManager.shutdown();
    }

    @SuppressWarnings("unchecked")
    public static List<Class<? extends AbstractTask>> getAllTasks() {
        return Arrays.asList(allTasks);
    }

    @SuppressWarnings("unchecked")
    public static List<Class<? extends AbstractTask>> getTasksForTrigger(Task.Trigger trigger) {
        ArrayList<Class<? extends AbstractTask>> tasks = new ArrayList<>();
        for (Class<? extends AbstractTask> c : allTasks) {
            Task t = c.getAnnotation(Task.class);
            if (t.trigger() == trigger) {
                tasks.add(c);
            }
        }
        return tasks;
    }

    public static void runTaskByClass(Class<? extends AbstractTask> task) {
        try {
            Logger.println("Running task " + task.getSimpleName() + "...");
            task.cast(task.getConstructors()[0].newInstance()).run();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            System.err.println("Something went wrong trying to instantiate task " + task.getSimpleName());
            e.printStackTrace();
        }
    }
}
