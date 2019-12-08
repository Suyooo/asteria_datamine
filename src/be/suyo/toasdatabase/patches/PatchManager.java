package be.suyo.toasdatabase.patches;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.tasks.ShutdownManager;
import be.suyo.toasdatabase.units.Unit;
import be.suyo.toasdatabase.units.UnitException;
import be.suyo.toasdatabase.units.UnitManager;

public class PatchManager {
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("addpatch")) {
            if (args.length < 4) {
                System.err.println("Usage: PatchManager addpatch [ID] [name] [date]");
            } else {
                Logger.println(
                        "Patch ID " + createPatch(Integer.parseInt(args[1]), args[2], args[3]).patchId + " added");
            }
        } else if (args.length >= 1 && args[0].equals("addcat")) {
            if (args.length < 4) {
                System.err.println("Usage: PatchManager addcat [patch ID] [name or NULL] [order]");
            } else {
                Logger.println("Category ID " + createCategory(Patch.get(Integer.parseInt(args[1])), args[2],
                        Integer.parseInt(args[3])).pcatId + " added");
            }
        } else if (args.length >= 1 && args[0].equals("addunit")) {
            if (args.length < 4) {
                System.err.println("Usage: PatchManager addunit [category ID] [name or NULL] [order]");
            } else {
                Logger.println("Unit ID " +
                        createPendingUnit(Integer.parseInt(args[1]), PatchUnit.Change.valueOf(args[2]),
                                PatchCategory.get(Integer.parseInt(args[3]))).punitUnitId + " added");
            }
        } else {
            System.err.println("Possible commands: addpatch, addcat, addunit");
        }
        ShutdownManager.shutdown();
    }

    @SuppressWarnings("deprecation")
    public static Patch createPatch(int patchId, String name, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));

        Patch p = new Patch();
        p.patchName = name;
        try {
            p.patchDate = dateFormat.parse(date);
            p.patchDate.setHours(p.patchDate.getHours() + 16);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        p.patchId = patchId;
        Patch.save(p);
        return p;
    }

    public static PatchCategory createCategory(Patch patch, String name, int order) {
        PatchCategory c = new PatchCategory();
        c.pcatPatch = patch;
        c.pcatName = (name == null || name.equals("NULL")) ? null : name;
        c.pcatOrder = order;
        PatchCategory.save(c);
        return c;
    }

    public static PatchUnit createPendingUnit(int unitId, PatchUnit.Change change, PatchCategory cat) {
        PatchUnit u = new PatchUnit();
        u.punitUnitId = unitId;
        u.punitChange = change;
        u.punitCategory = cat;
        u.punitPending = true;
        PatchUnit.save(u);
        return u;
    }

    public static void checkPendingUnits() {
        List<PatchUnit> pendingUnits = PatchUnit.getPending();

        for (PatchUnit pu : pendingUnits) {
            if (pu.punitChange == PatchUnit.Change.NEW_UNIT) {
                try {
                    UnitManager.downloadUnit(pu.punitUnitId);
                    pu.punitPending = false;
                    PatchUnit.save(pu);
                    Logger.println("New Unit (ID " + pu.punitUnitId + ") successfully updated");
                } catch (UnitException e) {
                    if (e.getMessage().startsWith("Invalid Unit ID")) {
                        Logger.println("New Unit (ID " + pu.punitUnitId + ") not available yet");
                    } else {
                        System.err.println("New Unit (ID " + pu.punitUnitId + ") failed: ");
                        System.err.println("    " + e.getMessage());
                    }
                }
            } else if (pu.punitChange == PatchUnit.Change.STATS_CHANGED || pu.punitChange == PatchUnit.Change.BUFFED ||
                    pu.punitChange == PatchUnit.Change.NERFED) {
                Unit puu = Unit.get(pu.punitUnitId);
                assert puu != null;
                int previousStatHash = puu.statHashCode();
                try {
                    Unit u = UnitManager.downloadUnit(pu.punitUnitId);
                    if (previousStatHash == u.statHashCode()) {
                        Logger.println("Changed Unit (ID " + pu.punitUnitId + ") not changed yet");
                    } else {
                        pu.punitPending = false;
                        PatchUnit.save(pu);
                        Logger.println("Changed Unit (ID " + pu.punitUnitId + ") successfully updated");
                    }
                } catch (UnitException e) {
                    System.err.println("Changed Unit (ID " + pu.punitUnitId + ") failed: ");
                    System.err.println("    " + e.getMessage());
                }
            } else if (pu.punitChange == PatchUnit.Change.EVOLUTION_ADDED) {
                int baseUnitId = pu.punitUnitId - 1;
                try {
                    Unit u = UnitManager.downloadUnit(baseUnitId);
                    if (u.unitExType != Unit.ExType.EVOLUTION) {
                        Logger.println("Evo Unit (ID " + pu.punitUnitId + ") not evolved yet");
                    } else {
                        try {
                            UnitManager.downloadUnit(pu.punitUnitId);
                            pu.punitPending = false;
                            PatchUnit.save(pu);
                            Logger.println("Evo Unit (ID " + pu.punitUnitId + ") successfully updated");
                        } catch (UnitException e) {
                            System.err.println("Evo Unit (ID " + pu.punitUnitId + ") failed: ");
                            System.err.println("    " + e.getMessage());
                        }
                    }
                } catch (UnitException e) {
                    System.err.println("Evo Base Unit (ID " + baseUnitId + ") failed: ");
                    System.err.println("    " + e.getMessage());
                }
            } else if (pu.punitChange == PatchUnit.Change.BOND_AWAKENING_ADDED) {
                int baseUnitId = pu.punitUnitId - 10;
                try {
                    Unit u = UnitManager.downloadUnit(pu.punitUnitId);
                    if (u.unitExType != Unit.ExType.BOND_AWAKENING) {
                        Logger.println("BAW Unit (ID " + pu.punitUnitId + ") not bondable yet");
                    } else {
                        try {
                            UnitManager.downloadUnit(baseUnitId);
                            pu.punitPending = false;
                            PatchUnit.save(pu);
                            Logger.println("BAW Unit (ID " + pu.punitUnitId + ") successfully updated");
                        } catch (UnitException e) {
                            System.err.println("BAW Base Unit (ID " + baseUnitId + ") failed: ");
                            System.err.println("    " + e.getMessage());
                        }
                    }
                } catch (UnitException e) {
                    System.err.println("BAW Unit (ID " + pu.punitUnitId + ") failed: ");
                    System.err.println("    " + e.getMessage());
                }
            } else if (pu.punitChange == PatchUnit.Change.DUAL_MA_ADDED) {
                try {
                    Unit u = UnitManager.downloadUnit(pu.punitUnitId);
                    if (u.unitDualMa == null) {
                        Logger.println("Dual MA of Unit (ID " + pu.punitUnitId + ") not available yet");
                    } else {
                        pu.punitPending = false;
                        PatchUnit.save(pu);
                        Logger.println("Dual MA of Unit (ID " + pu.punitUnitId + ") successfully updated");
                    }
                } catch (UnitException e) {
                    System.err.println("Dual MA Unit (ID " + pu.punitUnitId + ") failed: ");
                    System.err.println("    " + e.getMessage());
                }
            }
        }
    }
}
