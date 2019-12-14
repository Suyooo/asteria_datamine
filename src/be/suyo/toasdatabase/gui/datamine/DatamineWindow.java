package be.suyo.toasdatabase.gui.datamine;

import be.suyo.toasdatabase.datamine.DatamineHelper;
import be.suyo.toasdatabase.gui.LoggingExecutingDialog;
import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.news.DatabaseStoreCallback;
import be.suyo.toasdatabase.news.NewsManager;
import be.suyo.toasdatabase.news.NewsPost;
import be.suyo.toasdatabase.news.NewsUpdateCallback;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatamineWindow extends BasicWindow {
    private ActionListBox actions;
    private Button exit;

    public DatamineWindow() {
        super("Datamine Tools");
        setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        actions = new ActionListBox();
        actions.addItem("1 Download Unit Images For Package", () -> {
            BigInteger nid = TextInputDialog
                    .showNumberDialog(getTextGUI(), "Download Unit Images For Package", "Enter Package ID", "");
            if (nid != null) {
                getTextGUI().addWindowAndWait(new LoggingExecutingDialog("Download Unit Images For Package",
                        () -> DatamineHelper.datamineHelper(nid.intValue())));
            }
        });
        actions.addItem("2 Check For New MAs", () -> getTextGUI()
                .addWindowAndWait(new LoggingExecutingDialog("Check For New MAs", DatamineHelper::doNewMaCheck)));
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
