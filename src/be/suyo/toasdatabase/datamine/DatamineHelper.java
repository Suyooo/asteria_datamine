package be.suyo.toasdatabase.datamine;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.packages.PackageDownloader;
import be.suyo.toasdatabase.packages.PackageUnit;
import be.suyo.toasdatabase.utils.Characters;
import be.suyo.toasdatabase.utils.DownloadException;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Global;
import be.suyo.toastoolkit.ccbi2ccb.CcbiFile;

import java.io.IOException;

public class DatamineHelper {

    public static void datamineHelper(int packageId) {
        try {
            for (PackageUnit unit : PackageDownloader.getListOfUnitsInPackage(packageId)) {
                if (unit.isEvolved) {
                    continue;
                }
                try {
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl(
                            "image/character/detail/character_detail_" + unit.shortName + "_" + (unit.unitId / 10) +
                                    ".png", true, "datamine_" + packageId);
                    Logger.notify("  New Unit " + Characters.getNameEn(unit.unitId / 10000) + " " + unit.unitId +
                            " (image downloaded)");
                } catch (DownloadException e) {
                    try {
                        DownloadUtils.downloadAndDecryptFileFromResourceUrl(
                                "image/soul/detail/detail_soul_" + unit.shortName + "_" + (unit.unitId / 10) + ".png",
                                true, "datamine_" + packageId);
                        Logger.notify("  New Soul " + Characters.getNameEn(unit.unitId / 10000) + " " + unit.unitId);
                    } catch (DownloadException e2) {
                        Logger.notify("  Thumb of unknown source " + Characters.getNameEn(unit.unitId / 10000) + " " +
                                unit.unitId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void doNewMaCheck() {
        int maCheckId = Integer.parseInt(Global.getValue("last_ma_id")) + 1;
        int lastDownloadedMa = -1;
        boolean lastMissed = false;

        while (true) {
            try {
                String ccbiFileName = "cocos_builder/ccb/evolve_spskill_cutin/ev" + maCheckId;
                DownloadUtils.downloadAndDecryptFileFromResourceUrl(ccbiFileName + ".ccbi", true, "maview");
                CcbiFile ccbi = new CcbiFile("maview/" + ccbiFileName + ".ccbi");

                ccbi.saveAsMAA("maview/ev" + maCheckId + ".js");

                for (String s : ccbi.getResourcesAtlas()) {
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl(s + ".png", true, "maview");
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl(s + ".plist", true, "maview");
                    be.suyo.toastoolkit.plistatlasconvert.Main.main(
                            new String[]{"maview/" + s + ".plist", "maview/" + s + ".json"});
                }

                for (String s : ccbi.getResourcesTexture()) {
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl(s, true, "maview");
                }

                for (String s : ccbi.getResourcesAudioSFX()) {
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl("sound/se/battle/" + s + ".ogg", true,
                            "maview");
                }

                for (String s : ccbi.getResourcesAudioVoice()) {
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl("sound/voice/" + s + ".ogg", true, "maview");
                }

                Logger.notify("New MA #" + maCheckId);
                lastMissed = false;
                lastDownloadedMa = maCheckId++;
            } catch (DownloadException e) {
                maCheckId++;
                if (lastMissed) {
                    break;
                } else {
                    lastMissed = true;
                }
            } catch (IOException e) {
                Logger.notify("Unable to convert MA #" + maCheckId);
                e.printStackTrace();
                maCheckId++;
            }
        }

        if (lastDownloadedMa != -1) {
            Global.setValue("last_ma_id", "" + lastDownloadedMa);
        }
    }
}
