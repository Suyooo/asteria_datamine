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

@DatabaseTable(tableName = "mysticartes_ex")
public class MysticArteEx {
    private static Dao<MysticArteEx, Integer> dao = DatabaseConnection.getDao(MysticArteEx.class);

    public static MysticArteEx get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(MysticArteEx maex) {
        try {
            dao.createOrUpdate(maex);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "maex_id", id = true)
    public int maexId;

    @DatabaseField(columnName = "maex_value")
    public int maexValue;

    @DatabaseField(columnName = "maex_hits")
    public int maexHits;

    @DatabaseField(columnName = "maex_target", dataType = DataType.ENUM_INTEGER)
    public Target maexTarget;

    @DatabaseField(columnName = "maex_charge")
    public int maexCharge;

    @DatabaseField(columnName = "maex_coop_value")
    public int maexCoopValue;

    @DatabaseField(columnName = "maex_voice_ready")
    public String maexVoiceReady;

    @DatabaseField(columnName = "maex_voice_use")
    public String maexVoiceUse;

    public void update(JSONObject json) {
        JSONObject jsonEx = json.getJSONObject("evolveInfo");
        JSONObject jsonDetail = jsonEx.getJSONArray("detailList").getJSONObject(0);
        this.maexId = json.getInt("id");
        this.maexValue = jsonDetail.getInt("value");
        this.maexHits = jsonEx.getInt("hitNum");
        this.maexTarget = Target.values()[jsonDetail.getInt("target")];
        this.maexCharge = jsonEx.getInt("maxOverLimitGauge");
        this.maexCoopValue = jsonEx.getJSONObject("summary").getInt("value");
        if (jsonEx.has("gaugeMaxVoice")) {
            this.maexVoiceReady = UnitManager.downloadResource(jsonEx.getString("gaugeMaxVoice") + ".ogg");
        }
        if (jsonEx.has("invokeVoice")) {
            this.maexVoiceUse = UnitManager.downloadResource(jsonEx.getString("invokeVoice") + ".ogg");
        }

        save(this);
    }

    public int statHashCode() {
        return Objects.hash(this.maexValue, this.maexHits, this.maexTarget, this.maexCharge, this.maexCoopValue);
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
