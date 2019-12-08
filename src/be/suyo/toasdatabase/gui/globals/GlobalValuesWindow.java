package be.suyo.toasdatabase.gui.globals;

import java.util.Arrays;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import be.suyo.toasdatabase.utils.Global;

public class GlobalValuesWindow extends BasicWindow {
    private ActionListBox actions;
    private Button exit;

    public GlobalValuesWindow() {
        super("Set Version / Configuration Values");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        actions = new ActionListBox();
        actions.addItem("1 Set Current App Version", () -> {
            String v = TextInputDialog.showDialog(getTextGUI(), "Set Current App Version", "Enter New App Version",
                    Global.getValue("current_version"));
            if (v != null) {
                Global.setValue("current_version", v);
            }
        });
        actions.addItem("2 Set Current Asset Package ID", () -> {
            String v = TextInputDialog.showDialog(getTextGUI(), "Set Current Asset Package ID", "Enter New Package ID",
                    Global.getValue("current_asset_id"));
            if (v != null) {
                Global.setValue("current_asset_id", v);
            }
        });
        actions.addItem("3 Set Player ID", () -> {
            String v = TextInputDialog.showDialog(getTextGUI(), "Set Database Player ID", "Enter New Player ID",
                    Global.getValue("player_id"));
            if (v != null) {
                Global.setValue("player_id", v);
            }
        });
        actions.addItem("4 Character Name List", () -> getTextGUI().addWindowAndWait(new CharactersWindow()));
        actions.addItem("5 Source Name List", () -> getTextGUI().addWindowAndWait(new SourcesWindow()));
        actions.addItem("6 Unit Subtitle List", () -> getTextGUI().addWindowAndWait(new SubtitlesWindow()));
        mainPanel.addComponent(actions, BorderLayout.Location.CENTER);

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
                if (taskIndex < actions.getItemCount()) {
                    actions.setSelectedIndex(taskIndex);
                    actions.getItemAt(taskIndex).run();
                }
            }
        }
        return super.handleInput(key);
    }
}
