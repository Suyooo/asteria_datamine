package be.suyo.toasdatabase.souls;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.units.Unit;
import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "soul_matches")
public class SoulMatch {
    private static Dao<SoulMatch, Integer> dao = DatabaseConnection.getDao(SoulMatch.class);

    private static PreparedQuery<SoulMatch> queryByUnitId;
    private static PreparedDelete<SoulMatch> deleteByUnitId;
    private static PreparedDelete<SoulMatch> deleteByUnitAndSoulId;
    private static SelectArg unitIdArg;
    private static SelectArg soulIdArg;

    static {
        try {
            unitIdArg = new SelectArg();
            soulIdArg = new SelectArg();

            QueryBuilder<SoulMatch, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("smch_unit_id", unitIdArg);
            queryByUnitId = queryBuilder.prepare();

            DeleteBuilder<SoulMatch, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("smch_unit_id", unitIdArg);
            deleteByUnitId = deleteBuilder.prepare();

            deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("smch_unit_id", unitIdArg);
            deleteBuilder.where().eq("smch_soul_id", soulIdArg);
            deleteByUnitAndSoulId = deleteBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Set<Integer> getMatchingSoulIDsForUnit(Unit u) {
        try {
            unitIdArg.setValue(u.unitId);
            Set<Integer> ret = new HashSet<>();
            for (SoulMatch sm : dao.query(queryByUnitId)) {
                ret.add(sm.smchSoul.soulId);
            }
            return ret;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void remove(Unit u, int soulGroupId) {
        try {
            unitIdArg.setValue(u.unitId);
            soulIdArg.setValue(soulGroupId);
            dao.delete(deleteByUnitAndSoulId);
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

    public static void save(SoulMatch sm) {
        try {
            dao.createOrUpdate(sm);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void refresh(SoulMatch sm) {
        try {
            dao.refresh(sm);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "smch_id", generatedId = true)
    public int smchId;

    @DatabaseField(columnName = "smch_unit_id", foreign = true, foreignAutoRefresh = true)
    public Unit smchUnit;

    @DatabaseField(columnName = "smch_soul_id", foreign = true, foreignAutoRefresh = true)
    public Soul smchSoul;

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
