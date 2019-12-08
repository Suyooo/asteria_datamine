package be.suyo.toasdatabase.partners;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "partners")
public class Partner {
    private static Dao<Partner, Integer> dao = DatabaseConnection.getDao(Partner.class);

    public static Partner get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(Partner p) {
        try {
            dao.createOrUpdate(p);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "part_unit_id", id = true)
    public int partUnitId;

    @DatabaseField(columnName = "part_image")
    public String partImage;

    @DatabaseField(columnName = "part_recent1", dataType = DataType.DATE_LONG)
    public Date partRecent1;

    @DatabaseField(columnName = "part_recent2", dataType = DataType.DATE_LONG)
    public Date partRecent2;

    @DatabaseField(columnName = "part_recent3", dataType = DataType.DATE_LONG)
    public Date partRecent3;

    @DatabaseField(columnName = "part_recent4", dataType = DataType.DATE_LONG)
    public Date partRecent4;

    @DatabaseField(columnName = "part_recent5", dataType = DataType.DATE_LONG)
    public Date partRecent5;

}
