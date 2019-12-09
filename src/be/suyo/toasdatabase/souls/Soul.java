package be.suyo.toasdatabase.souls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.souls.SoulException;
import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Multithread;
import be.suyo.toasdatabase.utils.Subtitle;

@DatabaseTable(tableName = "souls")
public class Soul {
    private static Dao<Soul, Integer> dao = DatabaseConnection.getDao(Soul.class);

    public static Soul get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<Soul> getIterator() {
        return dao.iterator();
    }

    public static void save(Soul s) {
        try {
            dao.createOrUpdate(s);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "soul_id", id = true)
    public int soulId;

    @DatabaseField(columnName = "soul_ingame_id")
    public int soulIngameId;

    @DatabaseField(columnName = "soul_subtitle_id", foreign = true)
    public Subtitle soulSubtitle;

    @DatabaseField(columnName = "soul_image")
    public String soulImage;

    @DatabaseField(columnName = "soul_type", dataType = DataType.ENUM_INTEGER)
    public SoulType soulType;

    @DatabaseField(columnName = "soul_availability")
    public int soulAvailability;

    @DatabaseField(columnName = "soul_skill_rarity1_id", foreign = true)
    public SoulSkill soulSkillRarity1;

    @DatabaseField(columnName = "soul_skill_rarity2_id", foreign = true)
    public SoulSkill soulSkillRarity2;

    @DatabaseField(columnName = "soul_skill_rarity3_id", foreign = true)
    public SoulSkill soulSkillRarity3;

    @DatabaseField(columnName = "soul_limited_logo")
    public String soulLimitedLogo;

    @DatabaseField(columnName = "soul_match_type", dataType = DataType.ENUM_INTEGER)
    public SoulMatchType soulMatchType;

    @ForeignCollectionField
    public ForeignCollection<SoulMatch> soulUnitMatches;

    private static final Pattern reSoulIngameId = Pattern.compile("\\(ID:(\\d\\d\\d\\d)\\)");

    public void update() {
        this.soulAvailability = 0;
        this.soulSkillRarity1 = this.soulSkillRarity2 = this.soulSkillRarity3 = null;
        boolean metaInfoSet = false;

        Set<Future<JSONObject>> jsonFutures = new HashSet<>();
        for (int rarity = 1; rarity <= 3; rarity++) {
            for (int element = 1; element <= 6; element++) {
                Map<String, String> params = new HashMap<>();
                params.put("soul_master_id", this.soulId + "" + rarity + "" + element);
                jsonFutures.add(Multithread.downloadAndDecryptJsonFromUrlAsync("soul/view_max_status", params));
            }
        }

        for (Future<JSONObject> jsonFuture : jsonFutures) {
            JSONObject json;
            try {
                json = jsonFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new SoulException("Soul Info Download failed", e);
            }
            if (!json.has("soul")) {
                continue;
            }

            File file = new File(
                    "assets/json/soul/" + this.soulId + "-r" + json.getJSONObject("soul").getInt("rarity") + "e" +
                            json.getJSONObject("soul").getInt("element") + ".json");
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new RuntimeException("mkdirs failed");
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.toString().getBytes());
            } catch (IOException e) {
                System.err.println("Saving original JSON failed, exception follows. Continuing with database entry.");
                e.printStackTrace();
            }
            json = json.getJSONObject("soul");

            if (!metaInfoSet) {
                Matcher matcherIngameId = reSoulIngameId.matcher(json.getString("name"));
                if (matcherIngameId.find()) {
                    this.soulIngameId = Integer.parseInt(matcherIngameId.group(1));
                    this.soulSubtitle = Subtitle.getSubtitleForUnitName(
                            json.getString("name").replace(matcherIngameId.group(), "").trim());
                } else {
                    this.soulIngameId = -1;
                    this.soulSubtitle = Subtitle.getSubtitleForUnitName(json.getString("name").trim());
                    Logger.notify("[WARNING] Soul " + this.soulId + " has no ingame ID");
                }
                this.soulImage = json.getString("image");
                DownloadUtils.downloadAndDecryptFileFromResourceUrl(json.getString("image"), true);
                this.soulType =
                        Soul.SoulType.values()[json.getJSONArray("passiveSkillList").getJSONObject(0).getInt("type")];
                if (json.has("limitedLogoImage") && !json.getString("limitedLogoImage").equals("")) {
                    this.soulLimitedLogo = json.getString("limitedLogoImage");
                    DownloadUtils.downloadAndDecryptFileFromResourceUrl(json.getString("limitedLogoImage"), true);
                }
                this.soulMatchType =
                        json.getBoolean("isAllGoodMatch") ? Soul.SoulMatchType.ALL : Soul.SoulMatchType.SOULMATES;
                metaInfoSet = true;
            }

            if (this.soulSkillRarity1 == null && json.getInt("rarity") == 1) {
                this.soulSkillRarity1 =
                        SoulManager.updateSoulSkill(json.getJSONArray("passiveSkillList").getJSONObject(0));
            } else if (this.soulSkillRarity2 == null && json.getInt("rarity") == 2) {
                this.soulSkillRarity2 =
                        SoulManager.updateSoulSkill(json.getJSONArray("passiveSkillList").getJSONObject(0));
            } else if (this.soulSkillRarity3 == null && json.getInt("rarity") == 3) {
                this.soulSkillRarity3 =
                        SoulManager.updateSoulSkill(json.getJSONArray("passiveSkillList").getJSONObject(0));
            }
            this.soulAvailability |= 1 << ((json.getInt("rarity") - 1) * 6 + (json.getInt("element") - 1));
        }

        if (this.soulAvailability == 0) {
            throw new SoulException("Invalid Soul Group ID " + this.soulId);
        }

        if (this.soulMatchType == Soul.SoulMatchType.SOULMATES && this.soulLimitedLogo != null) {
            Logger.notify("[WARNING] Soul ID " + this.soulId +
                    " is limited but does not match all, make sure it's not a special matching type");
        }

        save(this);
    }

    public enum SoulType {
        UNUSED_0, ATK_UP, DEF_UP, HP_UP, ATK_DEF_UP, ATK_HP_UP, DEF_HP_UP, HP_ATK_DEF_UP, HEAL_EVERY_TURN, UNUSED_9,
        REDUCE_DAMAGE, OL_AT_START
    }

    public enum SoulMatchType {
        SOULMATES, ALL, UNUSED_2, UNUSED_3, UNUSED_4, UNUSED_5, UNUSED_6, UNUSED_7, UNUSED_8, UNUSED_9, UNUSED_10,
        UNUSED_11, UNUSED_12, UNUSED_13, SYMPHONIA, UNUSED_15, UNUSED_16, UNUSED_17, UNUSED_18, UNUSED_19, UNUSED_20,
        UNUSED_21, VESPERIA, HEARTS
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }
            try {
                s.append("\n    ").append(field.getName()).append(" = ");
                s.append(field.get(this));
            } catch (IllegalAccessException ex) {
                s.append("<illegal access>");
            }
        }
        return s.toString();
    }
}
