package be.suyo.toasdatabase.utils;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.souls.SoulManager;
import be.suyo.toasdatabase.tasks.ShutdownManager;
import be.suyo.toasdatabase.units.UnitException;
import be.suyo.toasdatabase.units.UnitManager;

@SuppressWarnings("MismatchedReadAndWriteOfArray")
public class DownloadFromIds {
    private static int[] unitIds = {};
    private static int[] soulIds = {};

    public static void main(String[] args) {
        for (int unitId : unitIds) {
            Logger.println("UID " + unitId);
            try {
                UnitManager.downloadUnit(unitId);
            } catch (UnitException e) {
                System.err.println("    Failed: " + e.getMessage());
            }
        }
        for (int soulId : soulIds) {
            Logger.println("SID " + soulId);
            try {
                SoulManager.downloadSoul(soulId);
            } catch (UnitException e) {
                System.err.println("    Failed: " + e.getMessage());
            }
        }
        ShutdownManager.shutdown();
    }
}
