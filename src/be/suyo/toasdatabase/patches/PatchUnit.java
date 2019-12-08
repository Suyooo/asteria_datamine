package be.suyo.toasdatabase.patches;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "patch_units")
public class PatchUnit {
    private static Dao<PatchUnit, Integer> dao = DatabaseConnection.getDao(PatchUnit.class);

    private static PreparedQuery<PatchUnit> queryPendingUnits;

    private static PreparedQuery<PatchUnit> queryByPCatId;
    private static SelectArg queryByPCatIdArg;

    static {
        try {
            QueryBuilder<PatchUnit, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("punit_pending", true);
            queryPendingUnits = queryBuilder.prepare();

            queryBuilder = dao.queryBuilder();
            queryByPCatIdArg = new SelectArg();
            queryBuilder.where().eq("punit_category_id", queryByPCatIdArg);
            queryBuilder.orderBy("punit_unit_id", true);
            queryByPCatId = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static List<PatchUnit> getPending() {
        try {
            return dao.query(queryPendingUnits);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<PatchUnit> getByPCatId(int id) {
        try {
            queryByPCatIdArg.setValue(id);
            return dao.iterator(queryByPCatId);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(PatchUnit pu) {
        try {
            dao.createOrUpdate(pu);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void delete(PatchUnit pu) {
        try {
            dao.delete(pu);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "punit_id", generatedId = true)
    public int punitId;

    @DatabaseField(columnName = "punit_pending")
    public boolean punitPending;

    @DatabaseField(columnName = "punit_unit_id")
    public int punitUnitId;

    @DatabaseField(columnName = "punit_category_id", foreign = true)
    public PatchCategory punitCategory;

    @DatabaseField(columnName = "punit_change", dataType = DataType.ENUM_INTEGER)
    public Change punitChange;

    public enum Change {
        NEW_UNIT, STATS_CHANGED, BUFFED, NERFED, EVOLUTION_ADDED, BOND_AWAKENING_ADDED, DUAL_MA_ADDED
    }
}
