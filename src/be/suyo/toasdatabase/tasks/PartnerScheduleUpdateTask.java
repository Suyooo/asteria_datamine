package be.suyo.toasdatabase.tasks;

import be.suyo.toasdatabase.partners.PartnerManager;

@Task(trigger = Task.Trigger.MANUAL, name = "Update Partner Schedule")
public class PartnerScheduleUpdateTask extends AbstractTask {
    public void run() {
        PartnerManager.updateSchedule();
    }
}
