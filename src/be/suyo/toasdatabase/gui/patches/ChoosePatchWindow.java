package be.suyo.toasdatabase.gui.patches;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.j256.ormlite.dao.CloseableIterator;

import be.suyo.toasdatabase.packages.PackageDownloader;
import be.suyo.toasdatabase.patches.Patch;
import be.suyo.toasdatabase.patches.PatchManager;
import be.suyo.toasdatabase.utils.Global;

public class ChoosePatchWindow extends BasicWindow {
    private ActionListBox patches;
    private ArrayList<Patch> patchesContent;
    private Button exit;

    public ChoosePatchWindow() {
        super("Manage Patches");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));
        patchesContent = new ArrayList<>();

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        Label head = new Label("  PID    Date          Name");
        head.addStyle(SGR.BOLD);
        patches = new ActionListBox();
        refreshPatchList();
        mainPanel.addComponent(Panels.vertical(head, patches), BorderLayout.Location.CENTER);

        Button create = new Button("Create");
        create.setEnabled(false);
        Button from_package = new Button("Package");
        from_package.setEnabled(false);
        Button edit = new Button("Edit");
        edit.setEnabled(false);
        Button name = new Button("Name Change");
        name.setEnabled(false);
        Button delete = new Button("Delete");
        delete.setEnabled(false);
        mainPanel.addComponent(Panels.vertical(create, from_package, edit, name, delete), BorderLayout.Location.RIGHT);

        exit = new Button("Return", this::close);
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    private void refreshPatchList() {
        patches.clearItems();
        patchesContent.clear();

        int i = 1;
        CloseableIterator<Patch> pi = Patch.getIterator();
        try {
            while (pi.hasNext()) {
                Patch p = pi.next();
                String num = "  ";
                if (i <= 10) {
                    num = (i++ % 10) + " ";
                }
                patches.addItem(num + Global.pad(p.patchId, 7) +
                        Global.pad(new SimpleDateFormat("MMM d, yyyy").format(p.patchDate), 14) + p.patchName, () -> {
                    getTextGUI().addWindowAndWait(new EditPatchWindow(p));
                    refreshPatchList();
                });
                patchesContent.add(p);
            }
        } finally {
            try {
                pi.close();
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
            } else if (key.getCharacter() == 'c' || key.getCharacter() == 'C') {
                BigInteger pid =
                        TextInputDialog.showNumberDialog(getTextGUI(), "Create Blank Patch", "Enter Patch ID", "");
                if (pid != null) {
                    PatchManager.createPatch(pid.intValue(), "New Patch",
                            new SimpleDateFormat("MMM d, yyyy").format(new Date()));
                    refreshPatchList();
                }
                return true;
            } else if (key.getCharacter() == 'p' || key.getCharacter() == 'P') {
                BigInteger pid = TextInputDialog
                        .showNumberDialog(getTextGUI(), "Create Patch From Package", "Enter Package ID", "");
                if (pid != null) {
                    PackageDownloader.createPatchForPackage(pid.intValue());
                    refreshPatchList();
                }
                return true;
            } else if (key.getCharacter() == 'e' || key.getCharacter() == 'E') {
                patches.getSelectedItem().run();
            } else if (key.getCharacter() == 'n' || key.getCharacter() == 'N') {
                Patch p = patchesContent.get(patches.getSelectedIndex());
                String s = TextInputDialog.showDialog(getTextGUI(), "Rename Patch", "Enter New Name", p.patchName);
                if (s != null) {
                    p.patchName = s;
                    Patch.save(p);
                    refreshPatchList();
                }
            } else if (key.getCharacter() == 'd' || key.getCharacter() == 'D') {
                Patch p = patchesContent.get(patches.getSelectedIndex());
                Patch.delete(p);
                refreshPatchList();
            } else if (key.getCharacter() >= '0' && key.getCharacter() <= '9') {
                int taskIndex = (int) key.getCharacter() - (int) '1';
                if (taskIndex < 0) {
                    taskIndex = 9;
                }
                if (taskIndex < patches.getItemCount()) {
                    patches.setSelectedIndex(taskIndex);
                }
            }
        }
        return super.handleInput(key);
    }
}
