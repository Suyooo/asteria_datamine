package be.suyo.toasdatabase.packages;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import be.suyo.toasdatabase.utils.Characters;
import be.suyo.toasdatabase.utils.DownloadException;
import be.suyo.toastoolkit.ccbi2ccb.CcbiFile;
import org.json.JSONArray;
import org.json.JSONObject;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.patches.Patch;
import be.suyo.toasdatabase.patches.PatchCategory;
import be.suyo.toasdatabase.patches.PatchManager;
import be.suyo.toasdatabase.patches.PatchUnit;
import be.suyo.toasdatabase.tasks.ShutdownManager;
import be.suyo.toasdatabase.units.Unit;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Global;

public class PackageDownloader {
    public static void main(String[] args) {
        downloadNewPackages();
        ShutdownManager.shutdown();
    }

    public static boolean isNewPackageAvailable() {
        JSONObject adddata = DownloadUtils.downloadAndDecryptJsonFromUrl("api/getAdditionalDataList", new HashMap<>());
        JSONArray packages = adddata.getJSONArray("additional_data_list");
        int start_pid = Integer.parseInt(Global.getValue("current_asset_id"));
        for (int i = 0; i < packages.length(); i++) {
            JSONObject p = packages.getJSONObject(i);
            int pid = p.getInt("revision");
            if (pid > start_pid) {
                return true;
            }
        }
        return false;
    }

    public static List<Integer> downloadNewPackages() {
        JSONObject adddata = DownloadUtils.downloadAndDecryptJsonFromUrl("api/getAdditionalDataList", new HashMap<>());
        JSONArray packages = adddata.getJSONArray("additional_data_list");
        List<Integer> newPackages = new ArrayList<>();
        int start_pid = Integer.parseInt(Global.getValue("current_asset_id"));
        int new_pid = start_pid;
        for (int i = 0; i < packages.length(); i++) {
            JSONObject p = packages.getJSONObject(i);
            int pid = p.getInt("revision");
            Logger.println(pid);
            if (pid > start_pid) {
                if (pid > new_pid) {
                    new_pid = pid;
                }
                Logger.println("  Downloading... " + p.getString("url"));
                DownloadUtils.downloadUpdatePackage(pid, p.getString("url"));
                Logger.notify("New package " + pid + " downloaded.");
                newPackages.add(pid);
            }
        }
        if (start_pid < new_pid) {
            Global.setValue("current_asset_id", "" + new_pid);
        }
        return newPackages;
    }

    public static List<PackageUnit> getListOfUnitsInPackage(int packageId) throws IOException {
        List<PackageUnit> unitList = new ArrayList<>();
        ZipInputStream zip = new ZipInputStream(new FileInputStream("packages/" + packageId + ".zip"));
        ZipEntry e;
        while ((e = zip.getNextEntry()) != null) {
            if (e.isDirectory()) {
                continue;
            }
            if (e.getName().startsWith("assets/image/character/thumb/character_thumb_")) {
                unitList.add(new PackageUnit(Integer.parseInt(e.getName().substring(49, 56) + "0"),
                        e.getName().substring(45, 48), false));
            } else if (e.getName().startsWith("assets/image/item/thumb/item_thumb_7") && e.getName().length() == 58) {
                unitList.add(new PackageUnit(Integer.parseInt(e.getName().substring(47, 54) + "1"),
                        e.getName().substring(43, 46), true));
            }
        }
        zip.close();
        return unitList;
    }

    public static void createPatchForPackage(int packageId) {
        try {
            Map<Integer, PatchUnit.Change> newUnits = new HashMap<>();
            for (PackageUnit unit : getListOfUnitsInPackage(packageId)) {
                newUnits.put(unit.unitId,
                        unit.isEvolved ? PatchUnit.Change.EVOLUTION_ADDED : PatchUnit.Change.NEW_UNIT);
            }
            if (newUnits.size() == 0) return;

            for (Integer unitId : newUnits.keySet()) {
                Unit prevU = Unit.get(unitId);
                if (prevU != null) {
                    if (prevU.unitExType == Unit.ExType.AWAKENING && prevU.unitRarity == Unit.Rarity.AWAKENED) {
                        newUnits.put(unitId, PatchUnit.Change.BOND_AWAKENING_ADDED);
                    } else if (prevU.unitExType == Unit.ExType.BOND_AWAKENING &&
                            prevU.unitRarity == Unit.Rarity.AWAKENED) {
                        newUnits.put(unitId, PatchUnit.Change.DUAL_MA_ADDED);
                    } else {
                        newUnits.put(unitId, PatchUnit.Change.STATS_CHANGED);
                    }
                }
                if (unitId % 10 == 0) {
                    // if both base and evolved unit are added, overwrite the evolved unit's change reason
                    if (newUnits.containsKey(unitId + 1)) {
                        newUnits.put(unitId + 1, newUnits.get(unitId));
                    }
                }
            }

            Patch p = PatchManager.createPatch(packageId, "Uncategorized Patch (#" + packageId + ")",
                    new SimpleDateFormat("MMM d, yyyy").format(new Date()));
            PatchCategory c = PatchManager.createCategory(p, null, 0);
            for (HashMap.Entry<Integer, PatchUnit.Change> entry : newUnits.entrySet()) {
                PatchManager.createPendingUnit(entry.getKey(), entry.getValue(), c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
