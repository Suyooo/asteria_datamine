package be.suyo.toasdatabase.utils;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.logging.Logger;

@DatabaseTable(tableName = "unit_subtitles")
public class Subtitle {
    private static Dao<Subtitle, Integer> dao = DatabaseConnection.getDao(Subtitle.class);

    private static PreparedQuery<Subtitle> queryByJpSub;
    private static SelectArg jpSubArg;

    private static final Pattern reSubtitleInUnitName = Pattern.compile("【(.*?)】");

    static {
        try {
            QueryBuilder<Subtitle, Integer> queryBuilder = dao.queryBuilder();
            jpSubArg = new SelectArg();
            queryBuilder.where().eq("usub_jp", jpSubArg);
            queryByJpSub = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static CloseableIterator<Subtitle> getIterator() {
        return dao.iterator();
    }

    public static Subtitle getSubtitleForUnitName(String unitName) {
        try {
            Matcher matcherSubtitle = reSubtitleInUnitName.matcher(unitName);
            if (!matcherSubtitle.find()) {
                return null;
            }
            String sub = matcherSubtitle.group(1);
            jpSubArg.setValue(sub);
            Subtitle s = dao.queryForFirst(queryByJpSub);
            if (s == null) {
                s = new Subtitle();
                s.usubJp = s.usubEn = sub;
                dao.create(s);
                Logger.notify("New unit subtitle (" + sub + ", ID " + s.usubId + ") added, needs translation.");
            }
            return s;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void save(Subtitle s) {
        try {
            dao.createOrUpdate(s);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @DatabaseField(columnName = "usub_id", generatedId = true)
    public int usubId;

    @DatabaseField(columnName = "usub_jp")
    public String usubJp;

    @DatabaseField(columnName = "usub_en")
    public String usubEn;
}
