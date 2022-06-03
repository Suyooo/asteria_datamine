package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.datamine.DatamineHelper;
import be.suyo.toasdatabase.packages.PackageDownloader;
import be.suyo.toasdatabase.patches.PatchManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Task(trigger = Task.Trigger.UPDATE_AVAILABLE, name = "Download New Packages")
public class DownloadNewPackagesTask extends AbstractTask {
    public void run() {
        List<Integer> newPackages = PackageDownloader.downloadNewPackages();
        for (int packageId : newPackages) {
            DatamineHelper.datamineHelper(packageId);
            PackageDownloader.createPatchForPackage(packageId);
        }

        if (!newPackages.isEmpty()) {
            DatamineHelper.doNewMaCheck();
        }
    }
}
