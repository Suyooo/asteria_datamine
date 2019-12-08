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

@DatabaseTable(tableName = "mysticartes")
public class MysticArte {
    private static Dao<MysticArte, Integer> dao = DatabaseConnection.getDao(MysticArte.class);

    public static MysticArte get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(MysticArte ma) {
        try {
            dao.createOrUpdate(ma);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "ma_id", id = true)
    public int maId;

    @DatabaseField(columnName = "ma_name")
    public String maName;

    @DatabaseField(columnName = "ma_type", dataType = DataType.ENUM_INTEGER)
    public ArteType maType;

    @DatabaseField(columnName = "ma_element", dataType = DataType.ENUM_INTEGER)
    public Element maElement;

    @DatabaseField(columnName = "ma_value")
    public int maValue;

    @DatabaseField(columnName = "ma_hits")
    public int maHits;

    @DatabaseField(columnName = "ma_target", dataType = DataType.ENUM_INTEGER)
    public Target maTarget;

    @DatabaseField(columnName = "ma_charge")
    public int maCharge;

    @DatabaseField(columnName = "ma_coop_value")
    public int maCoopValue;

    @DatabaseField(columnName = "ma_voice_ready")
    public String maVoiceReady;

    @DatabaseField(columnName = "ma_voice_use")
    public String maVoiceUse;

    @DatabaseField(columnName = "ma_image_cutin")
    public String maImageCutin;

    public void update(JSONObject json) {
        JSONObject jsonDetail = json.getJSONArray("detailList").getJSONObject(0);
        this.maId = json.getInt("id");
        this.maName = json.getString("name");
        this.maType = ArteType.values()[jsonDetail.getInt("type")];
        this.maElement = Element.values()[json.getInt("element")];
        this.maValue = jsonDetail.getInt("value");
        this.maHits = json.getInt("hitNum");
        this.maTarget = Target.values()[jsonDetail.getInt("target")];
        this.maCharge = json.getInt("maxOverLimitGauge");
        this.maCoopValue = json.getJSONObject("summary").getInt("value");
        if (json.has("gaugeMaxVoice")) {
            this.maVoiceReady = UnitManager.downloadResource(json.getString("gaugeMaxVoice") + ".ogg");
        }
        if (json.has("invokeVoice")) {
            this.maVoiceUse = UnitManager.downloadResource(json.getString("invokeVoice") + ".ogg");
        }
        if (json.has("cutInImage")) {
            this.maImageCutin = UnitManager.downloadResource(json.getString("cutInImage"));
        }

        save(this);
    }

    public int statHashCode() {
        return Objects.hash(this.maValue, this.maHits, this.maTarget, this.maCharge, this.maCoopValue);
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
