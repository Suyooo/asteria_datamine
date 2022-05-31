package be.suyo.toasdatabase.units;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Objects;

import org.json.JSONObject;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "bond_potentials")
public class BondPotential {
    private static Dao<BondPotential, Integer> dao = DatabaseConnection.getDao(BondPotential.class);

    public static BondPotential get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(BondPotential bp) {
        try {
            dao.createOrUpdate(bp);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "bpot_id", id = true)
    public int bpotId;

    @DatabaseField(columnName = "bpot_type", dataType = DataType.ENUM_INTEGER)
    public PotentialType bpotType;

    @DatabaseField(columnName = "bpot_chance")
    public int bpotChance;

    @DatabaseField(columnName = "bpot_value1")
    public int bpotValue1;

    @DatabaseField(columnName = "bpot_value2")
    public int bpotValue2;

    @DatabaseField(columnName = "bpot_value3")
    public int bpotValue3;

    @DatabaseField(columnName = "bpot_target", dataType = DataType.ENUM_INTEGER)
    public Target bpotTarget;

    @DatabaseField(columnName = "bpot_turns")
    public int bpotTurns;

    public void update(JSONObject json) {
        this.bpotId = json.getInt("id");
        if (json.getInt("type") >= PotentialType.values().length) {
            throw new UnitException("Bond Potential Type #" + json.getInt("type") + " not in enum");
        }
        this.bpotType = PotentialType.values()[json.getInt("type")];
        this.bpotChance = json.getInt("invocationRate");
        this.bpotValue1 = json.getInt("value1");
        this.bpotValue2 = json.getInt("value2");
        this.bpotValue3 = json.getInt("value3");
        if (json.getInt("target") >= Target.values().length) {
            throw new UnitException("Target Type #" + json.getInt("target") + " not in enum");
        }
        this.bpotTarget = Target.values()[json.getInt("target")];
        this.bpotTurns = json.getInt("turn");

        save(this);
    }

    public int statHashCode() {
        return Objects.hash(this.bpotType, this.bpotChance, this.bpotValue1, this.bpotValue2, this.bpotValue3,
                this.bpotTarget, this.bpotTurns);
    }

    public enum PotentialType {
        UNUSED_0, POWER_UP, HIT_UP, HEAL, BUFF_ATK, BUFF_DEF, DEBUFF_ATK, DEBUFF_DEF, STATUS_CURE, POISON, PARALYSIS,
        UNUSED_11, BREAK_GAUGE_DMG_UP, CRIT_RATE_UP, CLEANSE_ATK_DEBUFF, CLEANSE_DEF_DEBUFF, UNUSED_16, SHIELD_DMG_UP,
        UNUSED_18, UNSEAL, GAIN_OL, UNUSED_21, BUFF_CRIT_DMG
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
