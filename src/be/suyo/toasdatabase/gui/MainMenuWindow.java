package be.suyo.toasdatabase.gui;

import java.math.BigInteger;
import java.util.Arrays;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import be.suyo.toasdatabase.gui.globals.GlobalValuesWindow;
import be.suyo.toasdatabase.gui.news.NewsWindow;
import be.suyo.toasdatabase.gui.patches.ChoosePatchWindow;
import be.suyo.toasdatabase.gui.tasks.TaskByNameWindow;
import be.suyo.toasdatabase.gui.tasks.TaskByTriggerWindow;
import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.souls.Soul;
import be.suyo.toasdatabase.souls.SoulManager;
import be.suyo.toasdatabase.units.Unit;
import be.suyo.toasdatabase.units.UnitManager;
import be.suyo.toasdatabase.utils.Global;

public class MainMenuWindow extends BasicWindow {
    private Label version;
    private ActionListBox actions;
    private Button exit;

    public MainMenuWindow() {
        super("Asteria Database GUI");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        version = new Label("");
        setVersionLabel();
        mainPanel.addComponent(version, BorderLayout.Location.TOP);

        actions = new ActionListBox();
        actions.addItem("1 Manage Patches", () -> getTextGUI().addWindowAndWait(new ChoosePatchWindow()));
        actions.addItem("2 Run Tasks By Name", () -> {
            getTextGUI().addWindowAndWait(new TaskByNameWindow());
            setVersionLabel();
        });
        actions.addItem("3 Run Tasks By Trigger", () -> {
            getTextGUI().addWindowAndWait(new TaskByTriggerWindow());
            setVersionLabel();
        });
        actions.addItem("4 Download Unit From ID", () -> {
            BigInteger uid =
                    TextInputDialog.showNumberDialog(getTextGUI(), "Download Unit From ID", "Enter Unit ID", "");
            if (uid != null) {
                getTextGUI().addWindowAndWait(new LoggingExecutingDialog("Download Unit From ID", () -> {
                    Logger.println("Downloading...");
                    Unit u = UnitManager.downloadUnit(uid.intValue());
                    Logger.println(u);
                }));
            }
        });
        actions.addItem("5 Download Soul From ID", () -> {
            BigInteger sid =
                    TextInputDialog.showNumberDialog(getTextGUI(), "Download Soul From ID", "Enter Soul Group ID", "");
            if (sid != null) {
                getTextGUI().addWindowAndWait(new LoggingExecutingDialog("Download Soul From ID", () -> {
                    Logger.println("Downloading...");
                    Soul s = SoulManager.downloadSoul(sid.intValue());
                    Logger.println(s);
                }));
            }
        });
        actions.addItem("6 Manage News", () -> {
            getTextGUI().addWindowAndWait(new NewsWindow());
            setVersionLabel();
        });
        actions.addItem("7 Set Version / Configuration Values / Global Names", () -> {
            getTextGUI().addWindowAndWait(new GlobalValuesWindow());
            setVersionLabel();
        });
        mainPanel.addComponent(actions, BorderLayout.Location.CENTER);

        exit = new Button("Exit", this::close);
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    private void setVersionLabel() {
        version.setText("Current Asset ID: " + Global.getValue("current_asset_id") + " (App Version " +
                Global.getValue("current_version") + ")\n ");
    }

    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'e' || key.getCharacter() == 'E') {
                exit.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            } else if (key.getCharacter() >= '0' && key.getCharacter() <= '9') {
                int taskIndex = (int) key.getCharacter() - (int) '1';
                if (taskIndex < 0) {
                    taskIndex = 9;
                }
                if (taskIndex < actions.getItemCount()) {
                    actions.setSelectedIndex(taskIndex);
                    actions.getItemAt(taskIndex).run();
                }
            }
        }
        return super.handleInput(key);
    }
}
