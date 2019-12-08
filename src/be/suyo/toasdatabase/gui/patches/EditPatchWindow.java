package be.suyo.toasdatabase.gui.patches;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

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

import be.suyo.toasdatabase.patches.IndexPageFeature;
import be.suyo.toasdatabase.patches.Patch;
import be.suyo.toasdatabase.patches.PatchCategory;
import be.suyo.toasdatabase.patches.PatchManager;
import be.suyo.toasdatabase.patches.PatchUnit;
import be.suyo.toasdatabase.utils.Characters;
import be.suyo.toasdatabase.utils.Global;

public class EditPatchWindow extends BasicWindow {
    private Patch patch;
    private int nextOrder;
    private ActionListBox patchContent;
    private ArrayList<PatchCategory> patchContentCats;
    private ArrayList<PatchUnit> patchContentUnits;
    private Button exit;

    public EditPatchWindow(Patch p) {
        super("Manage Patches");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        patch = p;
        patchContentCats = new ArrayList<>();
        patchContentUnits = new ArrayList<>();

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        patchContent = new ActionListBox();
        refreshPatchContent();
        mainPanel.addComponent(patchContent, BorderLayout.Location.CENTER);

        Button delete = new Button("Delete");
        delete.setEnabled(false);
        Button move = new Button("Move");
        move.setEnabled(false);
        Button add = new Button("Add Unit");
        add.setEnabled(false);
        Button pending = new Button("Pending");
        pending.setEnabled(false);
        Label change = new Label("← → Change");
        Button index = new Button("Index Feat.");
        index.setEnabled(false);
        Button create = new Button("Create Cat.");
        create.setEnabled(false);
        Button name = new Button("Name");
        name.setEnabled(false);
        Label orderhint = new Label("\n\n\n\nOrder:\nGacha\nEvent\nSpecial Ev.\nLogin Bonus\nRoulette\nAwk. Ptnr.s");
        mainPanel.addComponent(Panels.vertical(delete, move, add, pending, change, index, create, name, orderhint),
                BorderLayout.Location.RIGHT);

        exit = new Button("Return", this::close);
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    private void refreshPatchContent() {
        patchContent.clearItems();
        patchContentCats.clear();
        patchContentUnits.clear();

        CloseableIterator<PatchCategory> pci = PatchCategory.getByPatchId(patch.patchId);
        try {
            while (pci.hasNext()) {
                PatchCategory pc = pci.next();
                patchContent.addItem(Global.pad(pc.pcatOrder, 4) + pc.pcatName, () -> {
                });
                patchContentCats.add(pc);
                patchContentUnits.add(null);
                nextOrder = pc.pcatOrder + 1;
                CloseableIterator<PatchUnit> pui = PatchUnit.getByPCatId(pc.pcatId);
                try {
                    while (pui.hasNext()) {
                        PatchUnit pu = pui.next();
                        patchContent.addItem("  " + (pu.punitPending ? "P " : "  ") + Global.pad(pu.punitUnitId, 10) +
                                Global.pad(Characters.getNameEn(pu.punitUnitId / 10000), 30) + pu.punitChange, () -> {
                        });
                        patchContentCats.add(null);
                        patchContentUnits.add(pu);
                    }
                } finally {
                    try {
                        pui.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            try {
                pci.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean handleInput(KeyStroke key) {
        int selected = patchContent.getSelectedIndex();
        if (key.getKeyType() == KeyType.ArrowLeft) {
            PatchUnit pu = patchContentUnits.get(selected);
            if (pu != null) {
                int newChange = pu.punitChange.ordinal() - 1;
                if (newChange < 0) {
                    newChange = PatchUnit.Change.values().length - 1;
                }
                pu.punitChange = PatchUnit.Change.values()[newChange];
                PatchUnit.save(pu);
                refreshPatchContent();
                patchContent.setSelectedIndex(selected);
            }
        } else if (key.getKeyType() == KeyType.ArrowRight) {
            PatchUnit pu = patchContentUnits.get(selected);
            if (pu != null) {
                int newChange = pu.punitChange.ordinal() + 1;
                if (newChange >= PatchUnit.Change.values().length) {
                    newChange = 0;
                }
                pu.punitChange = PatchUnit.Change.values()[newChange];
                PatchUnit.save(pu);
                refreshPatchContent();
                patchContent.setSelectedIndex(selected);
            }
        } else if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'r' || key.getCharacter() == 'R') {
                exit.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            } else if (key.getCharacter() == 'd' || key.getCharacter() == 'D') {
                PatchUnit pu = patchContentUnits.get(selected);
                if (pu != null) {
                    PatchUnit.delete(pu);
                } else {
                    PatchCategory pc = patchContentCats.get(selected);
                    for (PatchUnit u : patchContentUnits) {
                        if (u == null) {
                            continue;
                        }
                        if (u.punitCategory.pcatId == pc.pcatId) {
                            PatchUnit.delete(pu);
                        }
                    }
                    if (pc.pcatOrder >= 1) {
                        for (PatchCategory c : patchContentCats) {
                            if (c == null) {
                                continue;
                            }
                            if (c.pcatOrder > pc.pcatOrder) {
                                c.pcatOrder--;
                                PatchCategory.save(c);
                            }
                        }
                    }
                    PatchCategory.delete(pc);
                }
                refreshPatchContent();
                return true;
            } else if (key.getCharacter() == 'm' || key.getCharacter() == 'M') {
                PatchUnit pu = patchContentUnits.get(selected);
                if (pu != null) {
                    BigInteger cid =
                            TextInputDialog.showNumberDialog(getTextGUI(), "Move Unit", "Enter Target Category", "");
                    if (cid != null) {
                        for (PatchCategory c : patchContentCats) {
                            if (c == null) {
                                continue;
                            }
                            if (c.pcatOrder == cid.intValue()) {
                                pu.punitCategory = c;
                                PatchUnit.save(pu);
                                refreshPatchContent();
                                break;
                            }
                        }
                    }
                } else {
                    PatchCategory pc = patchContentCats.get(selected);
                    BigInteger cid =
                            TextInputDialog.showNumberDialog(getTextGUI(), "Move Category", "Enter New Position", "");
                    if (cid != null) {
                        for (PatchCategory c : patchContentCats) {
                            if (c == null) {
                                continue;
                            }
                            if (c.pcatOrder > pc.pcatOrder) {
                                c.pcatOrder--;
                                PatchCategory.save(c);
                            }
                        }
                        pc.pcatOrder = -1;
                        for (PatchCategory c : patchContentCats) {
                            if (c == null) {
                                continue;
                            }
                            if (c.pcatOrder >= cid.intValue()) {
                                c.pcatOrder++;
                                PatchCategory.save(c);
                            }
                        }
                        pc.pcatOrder = cid.intValue();
                        PatchCategory.save(pc);
                        refreshPatchContent();
                    }
                }
                return true;
            } else if (key.getCharacter() == 'a' || key.getCharacter() == 'A') {
                BigInteger uid = TextInputDialog.showNumberDialog(getTextGUI(), "Add Unit", "Enter Unit ID", "");
                if (uid != null) {
                    int ci = getSelectedCategory();
                    PatchManager.createPendingUnit(uid.intValue(), PatchUnit.Change.NEW_UNIT, patchContentCats.get(ci));
                    refreshPatchContent();
                    patchContent.setSelectedIndex(ci);
                }
                return true;
            } else if (key.getCharacter() == 'p' || key.getCharacter() == 'P') {
                PatchUnit pu = patchContentUnits.get(selected);
                if (pu != null) {
                    pu.punitPending = !pu.punitPending;
                    PatchUnit.save(pu);
                    refreshPatchContent();
                    patchContent.setSelectedIndex(selected);
                }
                return true;
            } else if (key.getCharacter() == 'i' || key.getCharacter() == 'I') {
                BigInteger uid = TextInputDialog.showNumberDialog(getTextGUI(), "Set As Index Page Feature",
                        "Enter Feature ID (0 for main feature)", "0");
                if (uid != null) {
                    int ci = getSelectedCategory();
                    IndexPageFeature.setIndexPageFeature(uid.intValue(), patchContentCats.get(ci),
                            patchContentUnits.get(selected).punitUnitId);
                }
                return true;
            } else if (key.getCharacter() == 'c' || key.getCharacter() == 'C') {
                PatchManager.createCategory(patch, null, nextOrder);
                refreshPatchContent();
                return true;
            } else if (key.getCharacter() == 'n' || key.getCharacter() == 'N') {
                PatchCategory pc = patchContentCats.get(selected);
                if (pc != null) {
                    String s = TextInputDialog.showDialog(getTextGUI(), "Rename Category", "Enter New Name (or NULL)",
                            pc.pcatName == null ? "NULL" : pc.pcatName);
                    if (s != null) {
                        pc.pcatName = s.equals("NULL") ? null : s;
                        PatchCategory.save(pc);
                        refreshPatchContent();
                        patchContent.setSelectedIndex(selected);
                    }
                }
            } else if (key.getCharacter() >= '0' && key.getCharacter() <= '9') {
                int catId = key.getCharacter() - '0';
                int taskIndex = 0;
                while (taskIndex < patchContentCats.size()) {
                    if (patchContentCats.get(taskIndex) != null && patchContentCats.get(taskIndex).pcatOrder == catId) {
                        patchContent.setSelectedIndex(taskIndex);
                    }
                    taskIndex++;
                }
            }
        }
        return super.handleInput(key);

    }

    private int getSelectedCategory() {
        int selected = patchContent.getSelectedIndex();
        while (patchContentCats.get(selected) == null) {
            selected--;
        }
        return selected;
    }
}
