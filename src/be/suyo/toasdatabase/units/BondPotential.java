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
        this.bpotType = PotentialType.values()[json.getInt("type")];
        this.bpotChance = json.getInt("invocationRate");
        this.bpotValue1 = json.getInt("value1");
        this.bpotValue2 = json.getInt("value2");
        this.bpotValue3 = json.getInt("value3");
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
        UNUSED_11, BREAK_GAUGE_DMG_UP, CRIT_RATE_UP
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
