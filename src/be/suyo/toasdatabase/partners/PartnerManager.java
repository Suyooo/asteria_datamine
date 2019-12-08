package be.suyo.toasdatabase.partners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.utils.DownloadUtils;

public class PartnerManager {
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("M/d H:mm");
    static {
        timeFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
    }

    public static void main(String[] args) {
        updateSchedule();
    }

    @SuppressWarnings("deprecation")
    public static void updateSchedule() {
        Map<String, String> params = new HashMap<>();
        JSONObject json = DownloadUtils
            .downloadAndDecryptJsonFromUrl("map/show_partner_quest_schedule", params);

        PartnerSchedule.clear();

        JSONArray schedule = json.getJSONArray("partnerQuestTodayList");
        boolean isYearChange = false;
        if (schedule.length() > 0) {
            PartnerSchedule firstSchedule =
                handleScheduleEntry(schedule.getJSONObject(0));
            if (firstSchedule.schdTime.getDate() == 31
                && firstSchedule.schdTime.getMonth() == Calendar.DECEMBER)
                isYearChange = true;
            for (int i = 1; i < schedule.length(); i++)
                handleScheduleEntry(schedule.getJSONObject(i));
        }
        schedule = json.getJSONArray("partnerQuestTomorrowList");
        for (int i = 0; i < schedule.length(); i++) {
            PartnerSchedule currentSchedule =
                handleScheduleEntry(schedule.getJSONObject(i));
            if (isYearChange)
                currentSchedule.schdTime.setYear(currentSchedule.schdTime.getYear() + 1);
        }
    }

    @SuppressWarnings("deprecation")
    private static PartnerSchedule handleScheduleEntry(JSONObject schdJson) {
        Map<String, String> detailParams = new HashMap<>();
        detailParams.put("schedule_id", "" + schdJson.getInt("id"));
        JSONObject detailJson = DownloadUtils
            .downloadAndDecryptJsonFromUrl("map/show_partner_quest_detail", detailParams);

        Partner ptnr = Partner.get(detailJson.getInt("id"));
        if (ptnr == null) {
            ptnr = new Partner();
            ptnr.partUnitId = detailJson.getInt("id");
            ptnr.partRecent1 = ptnr.partRecent2 =
                ptnr.partRecent3 = ptnr.partRecent4 = ptnr.partRecent5 = null;
        }
        ptnr.partImage = detailJson.getString("bannerImage");
        DownloadUtils.downloadAndDecryptFileFromResourceUrl(
            detailJson.getString("bannerImage"), true);

        JSONArray partnerAwakens = detailJson.getJSONArray("evolveUnitList");
        Set<Integer> existingAwakens = PartnerMatch.getMatchingUnitIDsForPartner(ptnr);
        for (int i = 0; i < partnerAwakens.length(); i++) {
            int awkUnitId = partnerAwakens.getJSONObject(i).getInt("id");
            if (existingAwakens.contains(awkUnitId))
                continue;
            PartnerMatch pm = new PartnerMatch();
            pm.pmchPartnerUnitId = ptnr.partUnitId;
            pm.pmchAwakeningUnitId = awkUnitId;
            PartnerMatch.save(pm);
        }

        String timeString = schdJson.getString("datePeriod").replaceAll("\\(.\\)", "")
            .replaceAll("～.*", "");

        PartnerSchedule schd = new PartnerSchedule();
        schd.schdId = schdJson.getInt("id");
        schd.schdPartner = ptnr;
        try {
            schd.schdTime = timeFormat.parse(timeString);
            schd.schdTime.setYear(new Date().getYear());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (ptnr.partRecent1 == null || schd.schdTime.after(ptnr.partRecent1)) {
            ptnr.partRecent5 = ptnr.partRecent4;
            ptnr.partRecent4 = ptnr.partRecent3;
            ptnr.partRecent3 = ptnr.partRecent2;
            ptnr.partRecent2 = ptnr.partRecent1;
            ptnr.partRecent1 = schd.schdTime;
        }

        Partner.save(ptnr);
        PartnerSchedule.save(schd);
        Logger.println(schd);

        return schd;
    }
}
