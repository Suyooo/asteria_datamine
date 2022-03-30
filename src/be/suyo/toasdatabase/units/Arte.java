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

@DatabaseTable(tableName = "artes")
public class Arte {
    private static Dao<Arte, Integer> dao = DatabaseConnection.getDao(Arte.class);

    public static Arte get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(Arte a) {
        try {
            dao.createOrUpdate(a);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "arte_id", id = true)
    public int arteId;

    @DatabaseField(columnName = "arte_name")
    public String arteName;

    @DatabaseField(columnName = "arte_type", dataType = DataType.ENUM_INTEGER)
    public ArteType arteType;

    @DatabaseField(columnName = "arte_value")
    public int arteValue;

    @DatabaseField(columnName = "arte_hits")
    public int arteHits;

    @DatabaseField(columnName = "arte_target", dataType = DataType.ENUM_INTEGER)
    public Target arteTarget;

    @DatabaseField(columnName = "arte_chance")
    public int arteChance;

    @DatabaseField(columnName = "arte_max_level")
    public int arteMaxLevel;

    @DatabaseField(columnName = "arte_voice")
    public String arteVoice;

    public void update(JSONObject json) {
        JSONObject jsonDetail = json.getJSONArray("detailList").getJSONObject(0);
        this.arteId = json.getInt("id");
        this.arteName = json.getString("name");
        if (jsonDetail.getInt("type") >= ArteType.values().length) {
            throw new UnitException("Arte Type #" + jsonDetail.getInt("type") + " not in enum");
        }
        this.arteType = ArteType.values()[jsonDetail.getInt("type")];
        this.arteValue = jsonDetail.getInt("value");
        this.arteHits = json.getInt("hitNum");
        if (jsonDetail.getInt("target") >= Target.values().length) {
            throw new UnitException("Target Type #" + jsonDetail.getInt("target") + " not in enum");
        }
        this.arteTarget = Target.values()[jsonDetail.getInt("target")];
        this.arteChance = json.getInt("invocationRate");
        this.arteMaxLevel = json.getInt("level");
        if (json.has("invokeVoice")) {
            this.arteVoice = UnitManager.downloadResource(json.getString("invokeVoice") + ".ogg");
        }

        save(this);
    }

    public int statHashCode() {
        return Objects.hash(this.arteValue, this.arteHits, this.arteChance, this.arteTarget);
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
