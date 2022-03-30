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

@DatabaseTable(tableName = "ex_skills")
public class ExSkill {
    private static Dao<ExSkill, Integer> dao = DatabaseConnection.getDao(ExSkill.class);

    public static ExSkill get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(ExSkill ex) {
        try {
            dao.createOrUpdate(ex);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "ex_id", id = true)
    public int exId;

    @DatabaseField(columnName = "ex_type_effect", dataType = DataType.ENUM_INTEGER)
    public EffectType exTypeEffect;

    @DatabaseField(columnName = "ex_type_condition", dataType = DataType.ENUM_INTEGER)
    public ConditionType exTypeCondition;

    @DatabaseField(columnName = "ex_type_values", dataType = DataType.ENUM_INTEGER)
    public ValueType exTypeValue;

    @DatabaseField(columnName = "ex_value1")
    public int exValue1;

    @DatabaseField(columnName = "ex_value2")
    public int exValue2;

    @DatabaseField(columnName = "ex_value3")
    public int exValue3;

    @DatabaseField(columnName = "ex_max_level")
    public int exMaxLevel;

    @DatabaseField(columnName = "ex_voice")
    public String exVoice;

    public void update(JSONObject json) {
        this.exId = json.getInt("id");
        this.exTypeEffect = EffectType.values()[json.getInt("category")];
        this.exTypeCondition = ConditionType.values()[json.getInt("conditionType")];
        this.exTypeValue = ValueType.values()[json.getInt("valueType")];
        this.exMaxLevel = json.getInt("maxLevel");
        if (json.has("invokeVoice")) {
            this.exVoice = UnitManager.downloadResource(json.getString("invokeVoice") + ".ogg");
        }

        if (this.exTypeEffect.re == null) {
            throw new UnitException("No RegEx for EX Skill " + this.exTypeEffect + ", " + this.exTypeCondition + ", " +
                    this.exTypeValue + ": " + json.getString("description"));
        }
        Matcher match = this.exTypeEffect.re.matcher(json.getString("description"));
        if (match.find()) {
            if (match.groupCount() >= 1) {
                this.exValue1 = Integer.parseInt(match.group(1));
                if (match.groupCount() >= 2) {
                    this.exValue2 = Integer.parseInt(match.group(2));
                    if (match.groupCount() >= 3) {
                        this.exValue3 = Integer.parseInt(match.group(3));
                    }
                }
            }
        } else {
            throw new UnitException(
                    "RegEx doesn't fit for EX Skill " + this.exTypeEffect + ", " + this.exTypeCondition + ", " +
                            this.exTypeValue + ": " + json.getString("description"));
        }

        save(this);
    }

    public int statHashCode() {
        return Objects.hash(this.exTypeEffect, this.exTypeCondition, this.exValue1, this.exValue2, this.exValue3);
    }

    public enum EffectType {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, UNUSED_4, UNUSED_5, UNUSED_6, UNUSED_7, UNUSED_8, UNUSED_9, UNUSED_10,
        ATK_UP("攻撃力[をが](\\d*)"), DEF_UP("防御力[をが](\\d*)"), HP_UP("MAX HP[をが](\\d*)"),
        ATK_DEF_UP("攻撃力[をが](\\d*)%?、防御力[をが](\\d*)"), HP_ATK_UP("MAX HP[をが](\\d*)、攻撃力[をが](\\d*)"),
        HP_DEF_UP("MAX HP[をが](\\d*)、防御力[をが](\\d*)"), HP_ATK_DEF_UP("MAX HP[をが](\\d*)、攻撃力[をが](\\d*)%?、防御力[をが](\\d*)"),
        ATK_UP_DEF_DOWN("攻撃力[をが](\\d*)(?:.*?)防御力[をが](\\d*)"), UNUSED_19, UNUSED_20, UNUSED_21, UNUSED_22, UNUSED_23,
        UNUSED_24, UNUSED_25, UNUSED_26, UNUSED_27, UNUSED_28, UNUSED_29, UNUSED_30, UNUSED_31, UNUSED_32,
        OL_UP("OLゲージを(\\d*)"), CRIT_RATE_UP("クリティカル率を(\\d*)"), UNUSED_35, UNUSED_36, UNUSED_37, UNUSED_38, UNUSED_39,
        UNUSED_40, UNUSED_41, UNUSED_42, UNUSED_43, UNUSED_44, UNUSED_45, UNUSED_46, UNUSED_47, UNUSED_48, UNUSED_49,
        UNUSED_50, FRAME_ATK_UP("【攻撃力UP】の効果を(\\d*)"), FRAME_DEF_UP("【防御力UP】の効果を(\\d*)"),
        FRAME_HEAL_UP("【HP回復】の効果を(\\d*)"), FRAME_OL_UP("【OL増加】の効果を(\\d*)"),
        FRAME_ATK_DEF_UP("【攻撃力UP】の効果を(\\d*)%、【防御力UP】の効果を(\\d*)"),
        FRAME_HP_ATK_UP("【HP回復】の効果を(\\d*)%、【攻撃力UP】の効果を(\\d*)"), FRAME_ATK_OL_UP("【攻撃力UP】の効果を(\\d*)%、【OL増加】の効果を(\\d*)"),
        FRAME_HEAL_DEF_UP("【HP回復】の効果を(\\d*)%、【防御力UP】の効果を(\\d*)"),
        FRAME_DEF_OL_UP("【防御力UP】の効果を(\\d*)%、【OL増加】の効果を(\\d*)"), FRAME_HEAL_OL_UP("【HP回復】の効果を(\\d*)%、【OL増加】の効果を(\\d*)"),
        UNUSED_61, UNUSED_62, UNUSED_63, UNUSED_64, UNUSED_65, UNUSED_66, UNUSED_67, UNUSED_68, UNUSED_69, UNUSED_70,
        UNUSED_71, UNUSED_72, UNUSED_73, UNUSED_74, UNUSED_75, UNUSED_76, UNUSED_77, UNUSED_78, UNUSED_79, UNUSED_80,
        HEAL("HPを(\\d*)"), UNUSED_82, REVIVE("HP(\\d*)"), UNUSED_84, UNUSED_85, UNUSED_86, UNUSED_87, UNUSED_88,
        UNUSED_89, UNUSED_90, UNUSED_91, UNUSED_92, UNUSED_93, UNUSED_94, UNUSED_95, UNUSED_96, UNUSED_97, UNUSED_98,
        UNUSED_99, UNUSED_100, ATK_UP_AND_PSN_RES("攻撃力を(\\d*)アップし、状態異常「毒」に(\\d*)%の"), UNUSED_102, UNUSED_103,
        UNUSED_104, DEF_UP_AND_PARA_RES("防御力を(\\d*)アップし、状態異常「麻痺」に(\\d*)%の"), UNUSED_106,
        ATK_UP_AND_STUN_RES("攻撃力[をが](\\d*)アップし、状態異常「気絶」に(\\d*)%の耐性を持つ");
        public final Pattern re;

        EffectType() {
            re = null;
        }

        EffectType(String p) {
            re = Pattern.compile(p);
        }
    }

    public enum ConditionType {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, UNUSED_4, UNUSED_5, UNUSED_6, UNUSED_7, UNUSED_8, UNUSED_9, ALWAYS,
        UNUSED_11, UNUSED_12, UNUSED_13, UNUSED_14, UNUSED_15, UNUSED_16, UNUSED_17, UNUSED_18, UNUSED_19, UNUSED_20,
        FRAME_MATCH, UNUSED_22, UNUSED_23, UNUSED_24, UNUSED_25, UNUSED_26, UNUSED_27, UNUSED_28, UNUSED_29, UNUSED_30,
        HEALTH_CRITICAL, HEALTH_FULL, TRANSFORMATION, UNUSED_34, KNOCKED_OUT, UNUSED_36, UNUSED_37, UNUSED_38,
        UNUSED_39, UNUSED_40, TURN_START, UNUSED_42, UNUSED_43, FULL_HEALTH_AGAIN, BATTLE_START, UNUSED_46, UNUSED_47,
        UNUSED_48, UNUSED_49, UNUSED_50, DAMAGED, UNUSED_52, UNUSED_53, UNUSED_54, UNUSED_55, UNUSED_56, UNUSED_57,
        UNUSED_58, UNUSED_59, UNUSED_60, FOR_EACH_ENEMY
    }

    public enum ValueType {
        MIXED, PERCENTAGE, POINTS
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
