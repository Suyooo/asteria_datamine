package be.suyo.toasdatabase.patches;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "patches")
public class Patch {
    private static Dao<Patch, Integer> dao = DatabaseConnection.getDao(Patch.class);

    private static PreparedQuery<Patch> queryReverseOrder;

    static {
        try {
            QueryBuilder<Patch, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy("patch_id", false);
            queryReverseOrder = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Patch get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(Patch p) {
        try {
            dao.createOrUpdate(p);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void delete(Patch p) {
        try {
            CloseableIterator<PatchCategory> cats = PatchCategory.getByPatchId(p.patchId);
            while (cats.hasNext()) {
                PatchCategory.delete(cats.next());
            }
            dao.delete(p);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<Patch> getIterator() {
        try {
            return dao.iterator(queryReverseOrder);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "patch_id", id = true)
    public int patchId;

    @DatabaseField(columnName = "patch_name")
    public String patchName;

    @DatabaseField(columnName = "patch_date", dataType = DataType.DATE_LONG)
    public Date patchDate;

    @DatabaseField(columnName = "patch_comment")
    public String patchComment;
}
