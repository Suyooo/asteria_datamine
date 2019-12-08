package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.packages.PackageDownloader;
import be.suyo.toasdatabase.utils.DownloadException;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Global;
import be.suyo.toastoolkit.ccbi2ccb.CcbiFile;

import java.io.IOException;
import java.util.List;

@Task(trigger = Task.Trigger.UPDATE_AVAILABLE, name = "Download New Packages")
public class DownloadNewPackagesTask extends AbstractTask {
    public void run() {
        List<Integer> newPackages = PackageDownloader.downloadNewPackages();
        for (int packageId : newPackages) {
            PackageDownloader.datamineHelper(packageId);
        }

        if (!newPackages.isEmpty()) {
            int maStartCheckId = Integer.parseInt(Global.getValue("last_ma_id")) + 1;
            int maCheckId = maStartCheckId;

            while (true) {
                try {
                    String ccbiFileName = "cocos_builder/ccb/evolve_spskill_cutin/ev" + maCheckId;
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl(ccbiFileName + ".ccbi", true, "maview");
                    CcbiFile ccbi = new CcbiFile("maview/" + ccbiFileName + ".ccbi");

                    ccbi.saveAsMAA("maview/ev" + maCheckId + ".js");

                    for (String s : ccbi.getResourcesAtlas()) {
                        DownloadUtils.downloadAndDecryptFileFromResourceUrl(s + ".png", true, "maview");
                        DownloadUtils.downloadAndDecryptFileFromResourceUrl(s + ".plist", true, "maview");
                        be.suyo.toastoolkit.plistatlasconvert.Main
                                .main(new String[]{"maview/" + s + ".plist", "maview/" + s + ".json"});
                    }

                    for (String s : ccbi.getResourcesAudioSFX()) {
                        DownloadUtils
                                .downloadAndDecryptFileFromResourceUrl("sound/se/battle/" + s + ".ogg", true, "maview");
                    }

                    for (String s : ccbi.getResourcesAudioVoice()) {
                        DownloadUtils
                                .downloadAndDecryptFileFromResourceUrl("sound/voice/" + s + ".ogg", true, "maview");
                    }

                    Logger.notify("New MA #" + maCheckId);
                    maCheckId++;
                } catch (DownloadException e) {
                    break;
                } catch (IOException e) {
                    Logger.notify("Unable to convert MA #" + maCheckId);
                    e.printStackTrace();
                    maCheckId++;
                }
            }

            if (maCheckId > maStartCheckId) {
                Global.setValue("last_ma_id", "" + (maCheckId - 1));
            }
        }
    }
}
