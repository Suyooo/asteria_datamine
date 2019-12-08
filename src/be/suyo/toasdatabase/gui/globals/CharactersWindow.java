package be.suyo.toasdatabase.gui.globals;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import be.suyo.toasdatabase.utils.Characters;
import be.suyo.toasdatabase.utils.Global;

public class CharactersWindow extends BasicWindow {
    private ActionListBox charList;
    private Button exit;

    private static final Pattern reNameSubtitleSplit = Pattern.compile("^(.*?) ?(?:\\[(.*?)])?$");

    public CharactersWindow() {
        super("Manage Characters");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        charList = new ActionListBox();
        refreshCharList();
        mainPanel.addComponent(charList, BorderLayout.Location.CENTER);

        exit = new Button("Return", () -> {
            Characters.createCharFilterTemplate();
            close();
        });
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    private void refreshCharList() {
        charList.clearItems();

        CloseableIterator<Characters.Entry> ci = Characters.getIterator();
        assert ci != null;
        try {
            while (ci.hasNext()) {
                Characters.Entry c = ci.next();
                charList.addItem(Global.pad(c.charId, 6) + Global.pad(c.charShortEn, 16) + c.getFullNameEn(), () -> {
                    String v = TextInputDialog.showDialog(getTextGUI(), "Set English Name",
                            "Enter Name, use [] for subtitle, prefix S: to set short name", c.getFullNameEn());
                    if (v != null) {
                        if (v.startsWith("S:")) {
                            c.charShortEn = v.substring(2);
                            Characters.save(c);
                        } else {
                            Matcher name = reNameSubtitleSplit.matcher(v);
                            //name.find();
                            if (name.matches()) {
                                c.charNameEn = name.group(1);
                                if (name.groupCount() == 2) {
                                    c.charSubtitleEn = name.group(2);
                                } else {
                                    c.charSubtitleEn = null;
                                }
                                Characters.save(c);
                            }
                        }
                        int selected = charList.getSelectedIndex();
                        refreshCharList();
                        charList.setSelectedIndex(selected);
                    }
                });
                charList.addItem("      " + Global.pad(c.charShortJp, 16) + c.getFullNameJp(), () -> {
                    String v = TextInputDialog.showDialog(getTextGUI(), "Set Japanese Name",
                            "Enter Name, use [] for subtitle, prefix S: to set short name", c.getFullNameJp());
                    if (v != null) {
                        if (v.startsWith("S:")) {
                            c.charShortJp = v.substring(2);
                            Characters.save(c);
                        } else {
                            Matcher name = reNameSubtitleSplit.matcher(v);
                            //name.find();
                            if (name.matches()) {
                                c.charNameJp = name.group(1);
                                if (name.groupCount() == 2) {
                                    c.charSubtitleJp = name.group(2);
                                } else {
                                    c.charSubtitleJp = null;
                                }
                                Characters.save(c);
                            }
                        }
                        int selected = charList.getSelectedIndex();
                        refreshCharList();
                        charList.setSelectedIndex(selected);
                    }
                });
            }
        } finally {
            try {
                ci.close();
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
