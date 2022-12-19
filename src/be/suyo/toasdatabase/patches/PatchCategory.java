package be.suyo.toasdatabase.patches;

import java.sql.SQLException;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "patch_categories")
public class PatchCategory {
    private static Dao<PatchCategory, Integer> dao = DatabaseConnection.getDao(PatchCategory.class);

    private static PreparedQuery<PatchCategory> queryByPatchId;
    private static SelectArg queryByPatchIdArg;

    static {
        try {
            QueryBuilder<PatchCategory, Integer> queryBuilder = dao.queryBuilder();
            queryByPatchIdArg = new SelectArg();
            queryBuilder.where().eq("pcat_patch_id", queryByPatchIdArg);
            queryBuilder.orderBy("pcat_order", true);
            queryByPatchId = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static PatchCategory get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<PatchCategory> getByPatchId(int id) {
        try {
            queryByPatchIdArg.setValue(id);
            return dao.iterator(queryByPatchId);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(PatchCategory pc) {
        try {
            dao.createOrUpdate(pc);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void delete(PatchCategory pc) {
        try {
            CloseableIterator<PatchUnit> units = PatchUnit.getByPCatId(pc.pcatId);
            while (units.hasNext()) {
                PatchUnit.delete(units.next());
            }
            dao.delete(pc);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "pcat_id", generatedId = true)
    public int pcatId;

    @DatabaseField(columnName = "pcat_patch_id", foreign = true)
    public Patch pcatPatch;

    @DatabaseField(columnName = "pcat_order")
    public int pcatOrder;

    @DatabaseField(columnName = "pcat_name")
    public String pcatName;

    @DatabaseField(columnName = "pcat_comment")
    public String pcatComment;
}
