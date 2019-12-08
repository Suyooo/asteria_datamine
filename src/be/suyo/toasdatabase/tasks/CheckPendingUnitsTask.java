package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.patches.PatchManager;

@Task(trigger = Task.Trigger.QUARTER_HOUR, name = "Check Pending Units")
public class CheckPendingUnitsTask extends AbstractTask {
    public void run() {
        PatchManager.checkPendingUnits();
    }
}
