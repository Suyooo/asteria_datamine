package be.suyo.toasdatabase.souls;

import org.json.JSONObject;

public class SoulManager {
    public static Soul downloadSoul(int soul_group_id) {
        return updateSoul(soul_group_id);
    }

    static Soul updateSoul(int soul_group_id) {
        Soul soul = Soul.get(soul_group_id);
        if (soul == null) {
            soul = new Soul();
            soul.soulId = soul_group_id;
        }
        soul.update();
        return soul;
    }

    static SoulSkill updateSoulSkill(JSONObject json) {
        SoulSkill sskl = SoulSkill.get(json.getInt("id"));
        if (sskl == null)
            sskl = new SoulSkill();
        sskl.update(json);
        return sskl;
    }
}
