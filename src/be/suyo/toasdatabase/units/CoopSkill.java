package be.suyo.toasdatabase.units;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "coop_skills")
public class CoopSkill {
    private static Dao<CoopSkill, Integer> dao = DatabaseConnection.getDao(CoopSkill.class);

    public static CoopSkill get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(CoopSkill cs) {
        try {
            dao.createOrUpdate(cs);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "coop_id", id = true)
    public int coopId;

    @DatabaseField(columnName = "coop_name")
    public String coopName;

    @DatabaseField(columnName = "coop_element", dataType = DataType.ENUM_INTEGER)
    public Element coopElement;

    @DatabaseField(columnName = "coop_rarity")
    public int coopRarity;

    @DatabaseField(columnName = "coop_type", dataType = DataType.ENUM_INTEGER)
    public CoopSkillType coopType;

    @DatabaseField(columnName = "coop_value")
    public int coopValue;

    @DatabaseField(columnName = "coop_duration")
    public int coopDuration;

    @DatabaseField(columnName = "coop_cooldown")
    public int coopCooldown;

    @DatabaseField(columnName = "coop_variant_type", dataType = DataType.ENUM_INTEGER)
    public CoopVariantType coopVariant;

    @DatabaseField(columnName = "coop_variant_value1")
    public int coopVariantValue1;

    @DatabaseField(columnName = "coop_variant_value2")
    public int coopVariantValue2;

    @DatabaseField(columnName = "coop_icon_type")
    public int coopIconType;

    @DatabaseField(columnName = "coop_icon_variation")
    public int coopIconVariation;

    public void update(JSONObject json) {
        JSONObject jsonSummary = json.getJSONObject("summary");
        this.coopId = json.getInt("id");
        this.coopName = json.getString("name");
        this.coopElement = Element.values()[json.getInt("element")];
        this.coopRarity = json.getInt("rarity");
        this.coopType = CoopSkillType.values()[jsonSummary.getInt("type")];
        this.coopValue = (jsonSummary.has("value")) ? jsonSummary.getInt("value") : 0;
        this.coopDuration = json.getInt("duration");
        this.coopCooldown = json.getInt("recast");
        this.coopIconType = json.getInt("imageType");
        this.coopIconVariation = json.getInt("imageVariation");

        if (jsonSummary.has("variationMessage")) {
            String[] descriptionParts = json.getString("description").split("。");
            String variantDescription = descriptionParts[descriptionParts.length - 1];
            if (variantDescription.startsWith("※")) {
                variantDescription = descriptionParts[descriptionParts.length - 2];
            }
            for (CoopVariantType variant : CoopVariantType.values()) {
                if (variant.re == null) {
                    continue;
                }
                Matcher match = variant.re.matcher(variantDescription);
                if (match.matches()) {
                    this.coopVariant = variant;
                    if (match.groupCount() >= 1) {
                        this.coopVariantValue1 = Integer.parseInt(match.group(1));
                        if (match.groupCount() >= 2) {
                            this.coopVariantValue2 = Integer.parseInt(match.group(2));
                        }
                    }
                    break;
                }
            }
        } else {
            this.coopVariant = CoopVariantType.NONE;
        }

        save(this);
    }

    public int statHashCode() {
        return Objects.hash(this.coopType, this.coopCooldown, this.coopDuration, this.coopRarity, this.coopValue,
                this.coopVariant, this.coopVariantValue1, this.coopVariantValue2);
    }

    public enum CoopSkillType {
        UNUSED_0, ATTACK, UNUSED_2, UNUSED_3, UNUSED_4, SHOT, UNUSED_6, PATK_BOOST, UNUSED_8, MATK_BOOST, UNUSED_10,
        PDEF_BOOST, UNUSED_12, MDEF_BOOST, UNUSED_14, APPEAL_TARGET, HIDDEN_TARGET, CROSS_COUNTER, GUARD, HEAL,
        UNUSED_20, UNUSED_21, POISON_GUARD, UNUSED_23, UNUSED_24, UNUSED_25, UNUSED_26, UNISON_MATK_BOOST, UNUSED_28, UNUSED_29,
        UNUSED_30, UNUSED_31, UNUSED_32, UNUSED_33, UNUSED_34, UNUSED_35, UNUSED_36, UNUSED_37, UNUSED_38, UNUSED_39,
        UNUSED_40, UNUSED_41, UNUSED_42, UNUSED_43, UNUSED_44, UNUSED_45, UNUSED_46, UNUSED_47, UNUSED_48, UNUSED_49,
        PROTECT
    }

    public enum CoopVariantType {
        NONE, CHAIN_PLUS("さらに物理チェイン数が(\\d*?)増加する"), DRAIN("さらに与えたダメージに応じてHPが回復する"), HP_LOST("使用後、自分のHPが(\\d*?)%減少する"),
        LUCK_HEAL("さらに(\\d*?)%の確率で自分のHPが(\\d*?)%回復する"), REVENGE("さらに戦闘不能人数が多いほど威力が上がる"),
        VITAL_PINCH("さらに自分のHPが30%以下の時、威力が上がる");
        public final Pattern re;

        CoopVariantType() {
            re = null;
        }

        CoopVariantType(String p) {
            re = Pattern.compile(p);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
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
