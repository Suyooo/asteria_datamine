package be.suyo.toasdatabase.partners;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "partner_schedule")
public class PartnerSchedule {
    private static Dao<PartnerSchedule, Integer> dao = DatabaseConnection.getDao(PartnerSchedule.class);

    public static PartnerSchedule get(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(PartnerSchedule ps) {
        try {
            dao.createOrUpdate(ps);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void clear() {
        try {
            DeleteBuilder<PartnerSchedule, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.delete();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "schd_id", generatedId = true)
    public int schdId;

    @DatabaseField(columnName = "schd_partner_id", foreign = true)
    public Partner schdPartner;

    @DatabaseField(columnName = "schd_time", dataType = DataType.DATE_LONG)
    public Date schdTime;

    public String toString() {
        return this.schdId + ": " + this.schdTime + "\n  " + this.schdPartner.partUnitId;
    }
}
