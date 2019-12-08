package be.suyo.toasdatabase.partners;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.utils.DatabaseConnection;
import be.suyo.toasdatabase.utils.DatabaseException;

@DatabaseTable(tableName = "partner_matches")
public class PartnerMatch {
    private static Dao<PartnerMatch, Integer> dao = DatabaseConnection.getDao(PartnerMatch.class);

    private static PreparedQuery<PartnerMatch> queryByPartnerId;
    private static SelectArg queryByPartnerArg;

    static {
        try {
            QueryBuilder<PartnerMatch, Integer> queryBuilder = dao.queryBuilder();
            queryByPartnerArg = new SelectArg();
            queryBuilder.where().eq("pmch_partner_id", queryByPartnerArg);
            queryByPartnerId = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Set<Integer> getMatchingUnitIDsForPartner(Partner ptnr) {
        try {
            queryByPartnerArg.setValue(ptnr.partUnitId);
            Set<Integer> ret = new HashSet<>();
            for (PartnerMatch pm : dao.query(queryByPartnerId)) {
                ret.add(pm.pmchAwakeningUnitId);
            }
            return ret;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(PartnerMatch pm) {
        try {
            dao.createOrUpdate(pm);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "pmch_id", generatedId = true)
    public int pmchId;

    @DatabaseField(columnName = "pmch_awakening_id")
    public int pmchAwakeningUnitId;

    @DatabaseField(columnName = "pmch_partner_id")
    public int pmchPartnerUnitId;

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
