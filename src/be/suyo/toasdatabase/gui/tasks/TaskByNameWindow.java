package be.suyo.toasdatabase.gui.tasks;

import java.util.Arrays;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import be.suyo.toasdatabase.gui.LoggingExecutingDialog;
import be.suyo.toasdatabase.tasks.AbstractTask;
import be.suyo.toasdatabase.tasks.Task;
import be.suyo.toasdatabase.tasks.TaskManager;

public class TaskByNameWindow extends BasicWindow {
    private ActionListBox tasks;
    private Button exit;

    public TaskByNameWindow() {
        super("Run Tasks By Name");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        tasks = new ActionListBox();
        int i = 1;
        for (Class<? extends AbstractTask> c : TaskManager.getAllTasks()) {
            Task t = c.getAnnotation(Task.class);
            String num = "  ";
            if (i <= 10) {
                num = (i++ % 10) + " ";
            }
            tasks.addItem(num + t.name(), () -> getTextGUI()
                    .addWindowAndWait(new LoggingExecutingDialog(t.name(), () -> TaskManager.runTaskByClass(c))));
        }
        mainPanel.addComponent(tasks, BorderLayout.Location.CENTER);

        exit = new Button("Return", this::close);
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'r' || key.getCharacter() == 'R') {
                exit.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            } else if (key.getCharacter() >= '0' && key.getCharacter() <= '9') {
                int taskIndex = (int) key.getCharacter() - (int) '1';
                if (taskIndex < 0) {
                    taskIndex = 9;
                }
                if (taskIndex < tasks.getItemCount()) {
                    tasks.setSelectedIndex(taskIndex);
                    tasks.getItemAt(taskIndex).run();
                }
            }
        }
        return super.handleInput(key);
    }
}
