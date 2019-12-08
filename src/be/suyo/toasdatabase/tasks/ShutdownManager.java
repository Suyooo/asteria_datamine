package be.suyo.toasdatabase.tasks;

import java.util.LinkedList;
import java.util.List;

import be.suyo.toasdatabase.utils.Multithread;

public class ShutdownManager {
    private static List<AbstractTask> postCopyTasks = new LinkedList<>();
    
    public static void shutdown() {
        ProcessBuilder pb = new ProcessBuilder("./updatefiles.sh");
        Process p;
        try {
            p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (AbstractTask t : postCopyTasks) {
            t.run();
        }
        Multithread.shutdown();
    }
    
    public static void addPostCopyTask(AbstractTask t) {
        postCopyTasks.add(t);
    }
}
