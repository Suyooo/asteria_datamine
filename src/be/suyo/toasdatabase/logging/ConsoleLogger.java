package be.suyo.toasdatabase.logging;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import be.suyo.toasdatabase.tasks.AbstractTask;
import be.suyo.toasdatabase.tasks.ShutdownManager;

public class ConsoleLogger implements LoggerIFace {
    private static String mailAddress = "chris";
    private static List<String> notifications = new LinkedList<>();

    private static void sendNotifications() {
        ProcessBuilder pb = new ProcessBuilder("/usr/sbin/sendmail", mailAddress);
        Process p;
        try {
            p = pb.start();
            OutputStream stdin = p.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            writer.write("Subject: Asteria Database Notifications\n\n");
            writer.write("A recent run has created these notifications:\n\n");
            for (String n : notifications) {
                writer.write(n + "\n");
            }
            writer.flush();
            writer.close();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doPrint(String s) {
        System.out.print(s);
    }
    public void doPrintln(String s) {
        System.out.println(s);
    }
    public void doNotify(String s) {
        if (notifications.size() == 0) {
            ShutdownManager.addPostCopyTask(new AbstractTask() {
                public void run() {
                    sendNotifications();
                }
            });
        }
        notifications.add(s);
    }
}
