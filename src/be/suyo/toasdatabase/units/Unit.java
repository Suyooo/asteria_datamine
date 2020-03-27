package be.suyo.toasdatabase.units;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.suyo.toasdatabase.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.souls.Soul;
import be.suyo.toasdatabase.souls.SoulMatch;
import be.suyo.toasdatabase.souls.SoulManager;
import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;
import be.suyo.toasdatabase.utils.Subtitle;

@DatabaseTable(tableName = "units")
public class Unit {
    private static Dao<Unit, Integer> dao = DatabaseConnection.getDao(Unit.class);

    public static Unit get(int id) {
        try {
            Unit u = dao.queryForId(id);
            if (u == null) {
                return null;
            }
            if (u.unitSoulMatches != null) {
                for (SoulMatch sm : u.unitSoulMatches) {
                    SoulMatch.refresh(sm);
                }
            } else {
                dao.assignEmptyForeignCollection(u, "unitSoulMatches");
            }
            if (u.unitSubquests != null) {
                for (Subquest sq : u.unitSubquests) {
                    Subquest.refresh(sq);
                }
            } else {
                dao.assignEmptyForeignCollection(u, "unitSubquests");
            }
            return u;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<Unit> getIterator() {
        return dao.iterator();
    }

    public static void save(Unit u) {
        try {
            dao.createOrUpdate(u);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // META

    @DatabaseField(columnName = "unit_id", id = true)
    public int unitId;

    @DatabaseField(columnName = "unit_ingame_id")
    public int unitIngameId;

    @DatabaseField(columnName = "unit_subtitle_id", foreign = true, foreignAutoRefresh = true)
    public Subtitle unitSubtitle;

    @DatabaseField(columnName = "unit_name_full")
    public String unitNameFull;

    @DatabaseField(columnName = "unit_rarity", dataType = DataType.ENUM_INTEGER)
    public Rarity unitRarity;

    @DatabaseField(columnName = "unit_element", dataType = DataType.ENUM_INTEGER)
    public Element unitElement;

    @DatabaseField(columnName = "unit_max_level")
    public int unitMaxLevel;

    @DatabaseField(columnName = "unit_has_bonus_level")
    public boolean unitHasBonusLevel;

    @DatabaseField(columnName = "unit_game_id")
    public int unitGameId;

    @DatabaseField(columnName = "unit_description")
    public String unitDescription;

    @DatabaseField(columnName = "unit_cv")
    public String unitCv;

    public enum Rarity {
        UNUSED_0, ONE, TWO, THREE, FOUR, FIVE, AWAKENED
    }

    // EVOLUTION / AWAKENING INFO

    @DatabaseField(columnName = "unit_ex_type", dataType = DataType.ENUM_INTEGER)
    public ExType unitExType;

    public enum ExType {
        NONE, EVOLUTION, AWAKENING, BOND_AWAKENING, AWAKENING_PARTNER
    }

    // IMAGES

    @DatabaseField(columnName = "unit_image_detail")
    public String unitImageDetail;

    @DatabaseField(columnName = "unit_image_background")
    public String unitImageBackground;

    @DatabaseField(columnName = "unit_image_mypage")
    public String unitImageMypage;

    @DatabaseField(columnName = "unit_image_thumbnail")
    public String unitImageThumbnail;

    @DatabaseField(columnName = "unit_image_party")
    public String unitImageParty;

    @DatabaseField(columnName = "unit_image_list")
    public String unitImageList;

    @DatabaseField(columnName = "unit_image_shadow")
    public String unitImageShadow;

    // BASE STATS

    @DatabaseField(columnName = "unit_battle_hp")
    public int unitBattleHp;

    @DatabaseField(columnName = "unit_battle_atk")
    public int unitBattleAtk;

    @DatabaseField(columnName = "unit_battle_def")
    public int unitBattleDef;

    // ARTES

    @DatabaseField(columnName = "unit_arte1_id", foreign = true, foreignAutoRefresh = true)
    public Arte unitArte1;

    @DatabaseField(columnName = "unit_arte1_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitArte1BPot;

    @DatabaseField(columnName = "unit_arte2_id", foreign = true, foreignAutoRefresh = true)
    public Arte unitArte2;

    @DatabaseField(columnName = "unit_arte2_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitArte2BPot;

    @DatabaseField(columnName = "unit_arte3_id", foreign = true, foreignAutoRefresh = true)
    public Arte unitArte3;

    @DatabaseField(columnName = "unit_arte3_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitArte3BPot;

    // MYSTIC ARTE

    @DatabaseField(columnName = "unit_ma_id", foreign = true, foreignAutoRefresh = true)
    public MysticArte unitMa;

    @DatabaseField(columnName = "unit_ma_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitMaBPot;

    @DatabaseField(columnName = "unit_maex_id", foreign = true, foreignAutoRefresh = true)
    public MysticArteEx unitMaEx;

    @DatabaseField(columnName = "unit_maex_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitMaExBPot;

    // DUAL MA

    @DatabaseField(columnName = "unit_dualma_soul_id")
    public int unitDualMaSoulId;

    @DatabaseField(columnName = "unit_dualma_id", foreign = true, foreignAutoRefresh = true)
    public MysticArte unitDualMa;

    @DatabaseField(columnName = "unit_dualma_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitDualMaBPot;

    @DatabaseField(columnName = "unit_dualmaex_id", foreign = true, foreignAutoRefresh = true)
    public MysticArteEx unitDualMaEx;

    @DatabaseField(columnName = "unit_dualmaex_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitDualMaExBPot;

    // EX SKILL

    @DatabaseField(columnName = "unit_exskill_id", foreign = true, foreignAutoRefresh = true)
    public ExSkill unitExSkill;

    // TRANSFORMATION INFO

    @DatabaseField(columnName = "unit_exskill_transform_id", foreign = true, foreignAutoRefresh = true)
    public ExSkill unitExSkillTransform;

    @DatabaseField(columnName = "unit_tf_label")
    public String unitTfLabel;

    @DatabaseField(columnName = "unit_tf_basearte1_id", foreign = true, foreignAutoRefresh = true)
    public Arte unitTfBaseArte1;

    @DatabaseField(columnName = "unit_tf_basearte1_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitTfBaseArte1BPot;

    @DatabaseField(columnName = "unit_tf_basearte2_id", foreign = true, foreignAutoRefresh = true)
    public Arte unitTfBaseArte2;

    @DatabaseField(columnName = "unit_tf_basearte2_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitTfBaseArte2BPot;

    @DatabaseField(columnName = "unit_tf_basearte3_id", foreign = true, foreignAutoRefresh = true)
    public Arte unitTfBaseArte3;

    @DatabaseField(columnName = "unit_tf_basearte3_bpot_id", foreign = true, foreignAutoRefresh = true)
    public BondPotential unitTfBaseArte3BPot;

    // CO-OP BATTLE

    @DatabaseField(columnName = "unit_coop_type", dataType = DataType.ENUM_INTEGER)
    public CoopType unitCoopType;

    @DatabaseField(columnName = "unit_coop_skill_id", foreign = true, foreignAutoRefresh = true)
    public CoopSkill unitCoopSkill;

    @DatabaseField(columnName = "unit_coop_patk")
    public int unitCoopPAtk;

    @DatabaseField(columnName = "unit_coop_matk")
    public int unitCoopMAtk;

    @DatabaseField(columnName = "unit_coop_pdef")
    public int unitCoopPDef;

    @DatabaseField(columnName = "unit_coop_mdef")
    public int unitCoopMDef;

    @DatabaseField(columnName = "unit_coop_type_bonus_hp")
    public int unitCoopTypeBonusHp;

    @DatabaseField(columnName = "unit_coop_type_bonus_patk")
    public int unitCoopTypeBonusPAtk;

    @DatabaseField(columnName = "unit_coop_type_bonus_matk")
    public int unitCoopTypeBonusMAtk;

    @DatabaseField(columnName = "unit_coop_type_bonus_pdef")
    public int unitCoopTypeBonusPDef;

    @DatabaseField(columnName = "unit_coop_type_bonus_mdef")
    public int unitCoopTypeBonusMDef;

    // SOULS

    @ForeignCollectionField
    public ForeignCollection<SoulMatch> unitSoulMatches;

    // SUBQUESTS

    @ForeignCollectionField
    public ForeignCollection<Subquest> unitSubquests;

    public enum CoopType {
        UNUSED_0, ATTACK, DEFENSE, MAGIC
    }

    private static final Pattern reUnitIngameId = Pattern.compile("\\[キャラクターID:(\\d\\d\\d\\d\\d\\d)]");

    public void update(JSONObject completeJson) {
        JSONObject json = completeJson.getJSONObject("unit");

        // META

        this.unitId = json.getInt("unit_id");
        this.unitSubtitle = Subtitle.getSubtitleForUnitName(json.getString("name"));
        this.unitNameFull = json.getString("fullName");
        this.unitRarity = Rarity.values()[json.getInt("rank")];
        this.unitElement = Element.values()[json.getInt("element")];
        this.unitMaxLevel = json.getInt("levelCap");
        this.unitHasBonusLevel = json.getInt("bonusLevel") > 0;
        this.unitGameId = this.unitId / 1_000_000;
        this.unitDescription = json.getString("detail");
        this.unitCv = json.getString("characterVoice");

        Matcher matcherIngameId = reUnitIngameId.matcher(this.unitDescription);
        if (matcherIngameId.find()) {
            this.unitIngameId = Integer.parseInt(matcherIngameId.group(1));
        } else {
            Logger.notify("WARNING: No Ingame ID found on unit ID " + this.unitId);
        }

        // EVOLUTION / AWAKENING INFO

        if (json.getInt("evolveUnitType") == 0) {
            this.unitExType = ExType.NONE;
        } else if (json.getInt("evolveUnitType") == 3) {
            this.unitExType = ExType.AWAKENING_PARTNER;
        } else if (json.getBoolean("isLightEvolve")) {
            this.unitExType = ExType.EVOLUTION;
        } else if (json.getInt("soulEvolveType") == 0) {
            this.unitExType = ExType.AWAKENING;
        } else {
            this.unitExType = ExType.BOND_AWAKENING;
        }

        // IMAGES

        this.unitImageDetail = UnitManager.downloadResource(json.getString("image"));
        this.unitImageBackground = UnitManager.downloadResource(json.getString("imageBackground"));
        this.unitImageMypage = UnitManager.downloadResource(json.getString("image").replaceAll("detail", "mypage"));
        this.unitImageThumbnail = UnitManager.downloadResource(json.getString("thumbnailImage"));
        this.unitImageParty = UnitManager.downloadResource(json.getString("partyImage"));
        this.unitImageList = UnitManager.downloadResource(json.getString("listImage"));
        this.unitImageShadow = UnitManager.downloadResource(json.getString("imageShadow"));

        // BASE STATS

        this.unitBattleHp = json.getInt("health");
        this.unitBattleAtk = json.getInt("attack");
        this.unitBattleDef = json.getInt("defence");

        // ARTES

        JSONArray artes = json.getJSONArray("skillList");
        this.unitArte1 = this.unitArte2 = this.unitArte3 = null;
        this.unitArte1BPot = this.unitArte2BPot = this.unitArte3BPot = null;
        switch (artes.length()) {
            case 3:
                this.unitArte3 = UnitManager.updateArte(artes.getJSONObject(2));
                if (artes.getJSONObject(2).has("potentialSkill")) {
                    this.unitArte3BPot =
                            UnitManager.updateBondPotential(artes.getJSONObject(2).getJSONObject("potentialSkill"));
                }
            case 2:
                this.unitArte2 = UnitManager.updateArte(artes.getJSONObject(1));
                if (artes.getJSONObject(1).has("potentialSkill")) {
                    this.unitArte2BPot =
                            UnitManager.updateBondPotential(artes.getJSONObject(1).getJSONObject("potentialSkill"));
                }
            case 1:
                this.unitArte1 = UnitManager.updateArte(artes.getJSONObject(0));
                if (artes.getJSONObject(0).has("potentialSkill")) {
                    this.unitArte1BPot =
                            UnitManager.updateBondPotential(artes.getJSONObject(0).getJSONObject("potentialSkill"));
                }
        }

        // MYSTIC ARTE

        if (json.has("specialSkill")) {
            JSONObject maJson = json.getJSONObject("specialSkill");
            this.unitMa = UnitManager.updateMysticArte(maJson);
            if (maJson.has("potentialSkill")) {
                this.unitMaBPot = UnitManager.updateBondPotential(maJson.getJSONObject("potentialSkill"));
            }
            if (maJson.has("evolveInfo")) {
                this.unitMaEx = UnitManager.updateMysticArteEx(maJson);
                if (maJson.getJSONObject("evolveInfo").has("potentialSkill")) {
                    this.unitMaExBPot = UnitManager
                            .updateBondPotential(maJson.getJSONObject("evolveInfo").getJSONObject("potentialSkill"));
                }
            }
        } else {
            this.unitMa = null;
            this.unitMaEx = null;
            this.unitMaBPot = this.unitMaExBPot = null;
        }

        // DUAL MA

        if (json.has("soulSpecialSkill")) {
            this.unitDualMaSoulId = json.getJSONArray("linkageSoulList").getJSONObject(0).getInt("soulGroupId");

            JSONObject dualMaJson = json.getJSONObject("soulSpecialSkill");
            this.unitDualMa = UnitManager.updateMysticArte(dualMaJson);
            if (dualMaJson.has("potentialSkill")) {
                this.unitDualMaBPot = UnitManager.updateBondPotential(dualMaJson.getJSONObject("potentialSkill"));
            }
            if (dualMaJson.has("evolveInfo")) {
                this.unitDualMaEx = UnitManager.updateMysticArteEx(dualMaJson);
                if (dualMaJson.getJSONObject("evolveInfo").has("potentialSkill")) {
                    this.unitDualMaExBPot = UnitManager.updateBondPotential(
                            dualMaJson.getJSONObject("evolveInfo").getJSONObject("potentialSkill"));
                }
            }
        } else {
            this.unitDualMaSoulId = 0;
            this.unitDualMa = null;
            this.unitDualMaEx = null;
            this.unitDualMaBPot = this.unitDualMaExBPot = null;
        }

        // EX SKILL

        if (json.has("exSkill")) {
            this.unitExSkill = UnitManager.updateExSkill(json.getJSONObject("exSkill"));
        } else {
            this.unitExSkill = null;
        }

        // TRANSFORMATION INFO
        this.unitTfBaseArte1 = this.unitTfBaseArte2 = this.unitTfBaseArte3 = null;
        this.unitTfBaseArte1BPot = this.unitTfBaseArte2BPot = this.unitTfBaseArte3BPot = null;
        if (json.has("kamuiExSkill")) {
            this.unitExSkillTransform = UnitManager.updateExSkill(json.getJSONObject("kamuiExSkill"));
            this.unitTfLabel = json.getString("transformTypeLabel");

            JSONArray baseartes = json.getJSONArray("baseUnitSkillList");
            switch (baseartes.length()) {
                case 3:
                    this.unitTfBaseArte3 = UnitManager.updateArte(baseartes.getJSONObject(2));
                    if (baseartes.getJSONObject(2).has("potentialSkill")) {
                        this.unitTfBaseArte3BPot = UnitManager
                                .updateBondPotential(baseartes.getJSONObject(2).getJSONObject("potentialSkill"));
                    }
                case 2:
                    this.unitTfBaseArte2 = UnitManager.updateArte(baseartes.getJSONObject(1));
                    if (baseartes.getJSONObject(1).has("potentialSkill")) {
                        this.unitTfBaseArte2BPot = UnitManager
                                .updateBondPotential(baseartes.getJSONObject(1).getJSONObject("potentialSkill"));
                    }
                case 1:
                    this.unitTfBaseArte1 = UnitManager.updateArte(baseartes.getJSONObject(0));
                    if (baseartes.getJSONObject(0).has("potentialSkill")) {
                        this.unitTfBaseArte1BPot = UnitManager
                                .updateBondPotential(baseartes.getJSONObject(0).getJSONObject("potentialSkill"));
                    }
            }
        } else {
            this.unitExSkillTransform = null;
            this.unitTfLabel = null;
        }

        // CO-OP BATTLE

        this.unitCoopType = CoopType.values()[json.getInt("job")];
        if (json.has("raidSkill")) {
            this.unitCoopSkill = UnitManager.updateCoopSkill(json.getJSONObject("raidSkill"));
        } else {
            this.unitCoopSkill = null;
        }
        this.unitCoopPAtk = json.getInt("physicalAttack");
        this.unitCoopMAtk = json.getInt("magicalAttack");
        this.unitCoopPDef = json.getInt("physicalDefence");
        this.unitCoopMDef = json.getInt("magicalDefence");
        this.unitCoopTypeBonusHp = json.getInt("healthJobBonus");
        this.unitCoopTypeBonusPAtk = json.getInt("physicalAttackJobBonus");
        this.unitCoopTypeBonusMAtk = json.getInt("magicalAttackJobBonus");
        this.unitCoopTypeBonusPDef = json.getInt("physicalDefenceJobBonus");
        this.unitCoopTypeBonusMDef = json.getInt("magicalDefenceJobBonus");

        // SOULS

        if (json.has("goodMatchSoulList")) {
            JSONArray soulArray = json.getJSONArray("goodMatchSoulList");
            Set<Integer> existingMatches = SoulMatch.getMatchingSoulIDsForUnit(this);
            for (int i = 0; i < soulArray.length(); i++) {
                JSONObject soulJson = soulArray.getJSONObject(i);
                int soulGroupId = soulJson.getInt("soulGroupId");
                if (existingMatches.contains(soulGroupId)) {
                    existingMatches.remove(soulGroupId);
                    continue;
                }
                SoulMatch smch = new SoulMatch();
                smch.smchUnit = this;
                smch.smchSoul = Soul.get(soulGroupId);
                if (smch.smchSoul == null) {
                    smch.smchSoul = SoulManager.downloadSoul(soulGroupId);
                }
                SoulMatch.save(smch);
            }
            for (Integer removedSoul : existingMatches) {
                SoulMatch.remove(this, removedSoul);
            }
        } else {
            SoulMatch.removeAll(this);
        }

        // SUBQUESTS

        JSONArray subqArray = completeJson.getJSONArray("subQuestList");
        if (subqArray.length() > 0) {
            Set<Integer> existingSubquests = Subquest.getSubquestIDsForUnit(this);
            for (int i = 0; i < subqArray.length(); i++) {
                int subqId = subqArray.getJSONObject(i).getInt("id");
                if (existingSubquests.contains(subqId)) {
                    existingSubquests.remove(subqId);
                    continue;
                }
                UnitManager.updateSubquest(subqArray.getJSONObject(i), this);
            }
            for (Integer removedSubquest : existingSubquests) {
                Subquest.remove(removedSubquest);
            }
        } else {
            Subquest.removeAll(this);
        }

        save(this);
    }

    public int statHashCode() {
        int arte1 = (this.unitArte1 == null) ? 0 : this.unitArte1.statHashCode();
        int arte2 = (this.unitArte2 == null) ? 0 : this.unitArte2.statHashCode();
        int arte3 = (this.unitArte3 == null) ? 0 : this.unitArte3.statHashCode();
        int arte1b = (this.unitArte1BPot == null) ? 0 : this.unitArte1BPot.statHashCode();
        int arte2b = (this.unitArte2BPot == null) ? 0 : this.unitArte2BPot.statHashCode();
        int arte3b = (this.unitArte3BPot == null) ? 0 : this.unitArte3BPot.statHashCode();

        int ma = (this.unitMa == null) ? 0 : this.unitMa.statHashCode();
        int mab = (this.unitMaBPot == null) ? 0 : this.unitMaBPot.statHashCode();
        int maex = (this.unitMaEx == null) ? 0 : this.unitMaEx.statHashCode();
        int maexb = (this.unitMaExBPot == null) ? 0 : this.unitMaExBPot.statHashCode();

        int dma = (this.unitDualMa == null) ? 0 : this.unitDualMa.statHashCode();
        int dmab = (this.unitDualMaBPot == null) ? 0 : this.unitDualMaBPot.statHashCode();
        int dmaex = (this.unitDualMaEx == null) ? 0 : this.unitDualMaEx.statHashCode();
        int dmaexb = (this.unitDualMaExBPot == null) ? 0 : this.unitDualMaExBPot.statHashCode();

        int ex = (this.unitExSkill == null) ? 0 : this.unitExSkill.statHashCode();
        int extf = (this.unitExSkillTransform == null) ? 0 : this.unitExSkillTransform.statHashCode();

        int tfarte1 = (this.unitTfBaseArte1 == null) ? 0 : this.unitTfBaseArte1.statHashCode();
        int tfarte2 = (this.unitTfBaseArte2 == null) ? 0 : this.unitTfBaseArte2.statHashCode();
        int tfarte3 = (this.unitTfBaseArte3 == null) ? 0 : this.unitTfBaseArte3.statHashCode();
        int tfarte1b = (this.unitTfBaseArte1BPot == null) ? 0 : this.unitTfBaseArte1BPot.statHashCode();
        int tfarte2b = (this.unitTfBaseArte2BPot == null) ? 0 : this.unitTfBaseArte2BPot.statHashCode();
        int tfarte3b = (this.unitTfBaseArte3BPot == null) ? 0 : this.unitTfBaseArte3BPot.statHashCode();

        int coop = (this.unitCoopSkill == null) ? 0 : this.unitCoopSkill.statHashCode();

        return Objects
                .hash(this.unitExType, this.unitBattleHp, this.unitBattleAtk, this.unitBattleDef, arte1, arte2, arte3,
                        arte1b, arte2b, arte3b, ma, mab, maex, maexb, dma, dmab, dmaex, dmaexb, ex, extf, tfarte1,
                        tfarte2, tfarte3, tfarte1b, tfarte2b, tfarte3b, coop, this.unitCoopPAtk, this.unitCoopMAtk,
                        this.unitCoopPDef, this.unitCoopMDef);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }
            try {
                s.append(field.getName()).append(" = ");
                s.append(field.get(this)).append("\n");
            } catch (IllegalAccessException ex) {
                s.append("<illegal access>\n");
            }
        }
        s.append("Souls {");
        for (SoulMatch m : unitSoulMatches) {
            s.append(m.smchSoul.toString()).append(" ,");
        }
        s.append("\n}\nSubquests {");
        for (Subquest q : unitSubquests) {
            s.append(q.toString()).append(" ,");
        }
        s.append("\n}");
        return s.toString();
    }
}
