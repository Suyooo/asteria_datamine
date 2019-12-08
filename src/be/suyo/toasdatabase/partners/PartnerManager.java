package be.suyo.toasdatabase.partners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import be.suyo.toasdatabase.units.Unit;
import org.json.JSONArray;
import org.json.JSONObject;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.utils.DownloadUtils;

public class PartnerManager {
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("M/d H:mm");

    static {
        timeFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
    }

    public static void updatePartnerTable() {
        JSONObject json =
                DownloadUtils.downloadAndDecryptJsonFromUrl("map/show_partner_unit_drop_setting", new HashMap<>());
        JSONArray partners = json.getJSONArray("partnerUnitInfoList");
        for (int p = 0; p < partners.length(); p++) {
            JSONObject detailJson = partners.getJSONObject(p);
            Unit partner = Unit.get(detailJson.getInt("unitMasterId"));
            assert partner != null;

            JSONArray partnerAwakens = detailJson.getJSONArray("evolveUnitInfoList");
            Set<Integer> existingAwakens = PartnerMatch.getMatchingUnitIDsForPartner(partner);
            for (int i = 0; i < partnerAwakens.length(); i++) {
                int awkUnitId = partnerAwakens.getJSONObject(i).getInt("unitMasterId");
                if (existingAwakens.contains(awkUnitId)) {
                    continue;
                }
                PartnerMatch pm = new PartnerMatch();
                pm.pmchPartnerUnitId = partner.unitId;
                pm.pmchAwakeningUnitId = awkUnitId;
                PartnerMatch.save(pm);
            }
        }
    }
}
