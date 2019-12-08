package be.suyo.toasdatabase.souls;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.json.JSONObject;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "soul_skills")
public class SoulSkill {
    private static Dao<SoulSkill, Integer> dao = DatabaseConnection.getDao(SoulSkill.class);

    public static SoulSkill get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(SoulSkill ss) {
        try {
            dao.createOrUpdate(ss);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "sskl_id", id = true)
    public int ssklId;

    @DatabaseField(columnName = "sskl_value1")
    public int ssklValue1;

    @DatabaseField(columnName = "sskl_value2")
    public int ssklValue2;

    @DatabaseField(columnName = "sskl_value3")
    public int ssklValue3;

    public void update(JSONObject json) {
        this.ssklId = json.getInt("id");
        this.ssklValue1 = json.getInt("value1");
        this.ssklValue2 = json.getInt("value2");
        this.ssklValue3 = json.getInt("value3");

        save(this);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                s.append("\n        ").append(field.getName()).append(" = ");
                s.append(field.get(this));
            } catch (IllegalAccessException ex) {
                s.append("<illegal access>");
            }
        }
        return s.toString();
    }
}
