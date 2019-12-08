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
import be.suyo.toasdatabase.utils.Sources;

public class SourcesWindow extends BasicWindow {
    private ActionListBox srcList;
    private Button exit;

    public SourcesWindow() {
        super("Manage Sources");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        srcList = new ActionListBox();
        refreshSrcList();
        mainPanel.addComponent(srcList, BorderLayout.Location.CENTER);

        exit = new Button("Return", () -> {
            Sources.createSrcFilterTemplate();
            close();
        });
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    private void refreshSrcList() {
        srcList.clearItems();

        CloseableIterator<Sources.Entry> si = Sources.getIterator();
        assert si != null;
        try {
            while (si.hasNext()) {
                Sources.Entry s = si.next();
                srcList.addItem(Global.pad(s.srcId, 6) + Global.pad(s.srcShort, 16) + s.srcNameEn, () -> {
                    String v = TextInputDialog
                            .showDialog(getTextGUI(), "Set English Name", "Enter Name, prefix S: to set short name",
                                    s.srcNameEn);
                    if (v != null) {
                        if (v.startsWith("S:")) {
                            s.srcShort = v.substring(2);
                            Sources.save(s);
                        } else {
                            s.srcNameEn = v;
                            Sources.save(s);
                        }
                        int selected = srcList.getSelectedIndex();
                        refreshSrcList();
                        srcList.setSelectedIndex(selected);
                    }
                });
                srcList.addItem("                      " + s.srcNameJp, () -> {
                    String v = TextInputDialog
                            .showDialog(getTextGUI(), "Set Japanese Name", "Enter Name, prefix S: to set short name",
                                    s.srcNameJp);
                    if (v != null) {
                        if (v.startsWith("S:")) {
                            s.srcShort = v.substring(2);
                            Sources.save(s);
                        } else {
                            s.srcNameJp = v;
                            Sources.save(s);
                        }
                        int selected = srcList.getSelectedIndex();
                        refreshSrcList();
                        srcList.setSelectedIndex(selected);
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
