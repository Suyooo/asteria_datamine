package be.suyo.toasdatabase.units;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "subquests")
public class Subquest {
    private static Dao<Subquest, Integer> dao =
        DatabaseConnection.getDao(Subquest.class);

    private static PreparedQuery<Subquest> queryByUnitId;
    private static PreparedDelete<Subquest> deleteByUnitId;
    private static SelectArg unitIdArg;

    static {
        try {
            QueryBuilder<Subquest, Integer> queryBuilder = dao.queryBuilder();
            unitIdArg = new SelectArg();
            queryBuilder.where().eq("subq_unit_id", unitIdArg);
            queryByUnitId = queryBuilder.prepare();

            DeleteBuilder<Subquest, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("subq_unit_id", unitIdArg);
            deleteByUnitId = deleteBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Subquest get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    public static Set<Integer> getSubquestIDsForUnit (Unit u) {
        try {
            unitIdArg.setValue(u.unitId);
            Set<Integer> ret = new HashSet<>();
            for (Subquest sq : dao.query(queryByUnitId)) {
                ret.add(sq.subqId);
            }
            return ret;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    public static void remove(int subqId) {
        try {
            dao.deleteById(subqId);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    public static void removeAll(Unit u) {
        try {
            unitIdArg.setValue(u.unitId);
            dao.delete(deleteByUnitId);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(Subquest sq) {
        try {
            dao.createOrUpdate(sq);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void refresh(Subquest sq) {
        try {
            dao.refresh(sq);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "subq_id", id = true)
    public int subqId;

    @DatabaseField(columnName = "subq_name")
    public String subqName;

    @DatabaseField(columnName = "subq_unit_id", foreign = true, foreignAutoRefresh = true)
    public Unit subqUnit;

    @DatabaseField(columnName = "subq_level")
    public int subqLevel;

    public void update(JSONObject json, Unit unit) {
        this.subqId = json.getInt("id");
        this.subqName = json.getString("name");
        this.subqUnit = unit;
        this.subqLevel = json.getInt("unlockLevel");

        save(this);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                s.append("\n    ").append(field.getName()).append(" = ");
                if (field.getName().equals("subqUnit"))
                    s.append(subqUnit.unitId);
                else
                    s.append(field.get(this));
            } catch (IllegalAccessException ex) {
                s.append("<illegal access>");
            }
        }
        return s.toString();
    }
}
