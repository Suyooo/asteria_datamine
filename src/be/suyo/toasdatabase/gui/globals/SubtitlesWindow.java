package be.suyo.toasdatabase.gui.globals;

import java.io.IOException;
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
import com.j256.ormlite.dao.CloseableIterator;

import be.suyo.toasdatabase.utils.Global;
import be.suyo.toasdatabase.utils.Subtitle;

public class SubtitlesWindow extends BasicWindow {
    private ActionListBox subList;
    private Button exit;

    public SubtitlesWindow() {
        super("Manage Subtitles");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        subList = new ActionListBox();
        refreshSubList();
        mainPanel.addComponent(subList, BorderLayout.Location.CENTER);

        exit = new Button("Return", this::close);
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    private void refreshSubList() {
        subList.clearItems();

        CloseableIterator<Subtitle> si = Subtitle.getIterator();
        try {
            while (si.hasNext()) {
                Subtitle s = si.next();
                subList.addItem(Global.pad(s.usubId, 4) + Global.pad(s.usubJp, 30) + s.usubEn, () -> {
                    String v = TextInputDialog
                            .showDialog(getTextGUI(), "Set English Subtitle", "Enter Subtitle", s.usubEn);
                    if (v != null) {
                        s.usubEn = v;
                        Subtitle.save(s);
                        int selected = subList.getSelectedIndex();
                        refreshSubList();
                        subList.setSelectedIndex(selected);
                    }
                });
            }
        } finally {
            try {
                si.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'r' || key.getCharacter() == 'R') {
                exit.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            }
        }
        return super.handleInput(key);

    }
}
