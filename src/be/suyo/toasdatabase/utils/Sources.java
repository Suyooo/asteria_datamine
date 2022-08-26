package be.suyo.toasdatabase.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.logging.Logger;

public class Sources {
    private static Dao<Entry, Integer> dao = DatabaseConnection.getDao(Entry.class);

    private static PreparedQuery<Entry> queryOrderById;

    static {
        try {
            QueryBuilder<Entry, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy("src_id", true);
            queryOrderById = queryBuilder.prepare();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static CloseableIterator<Entry> getIterator() {
        try {
            return dao.iterator(queryOrderById);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void save(Entry s) {
        try {
            dao.createOrUpdate(s);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static String getSourceNameEn(int sourceId) {
        try {
            Entry e = dao.queryForId(sourceId);
            if (e == null) {
                e = new Entry(sourceId);
                dao.create(e);
            }
            return e.srcNameEn;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static String getSourceNameShort(int sourceId) {
        try {
            Entry e = dao.queryForId(sourceId);
            if (e == null) {
                e = new Entry(sourceId);
                dao.create(e);
            }
            return e.srcShort;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static List<Integer> getSortedSourceIDList() {
        try {
            List<Integer> res = new LinkedList<>();
            QueryBuilder<Entry, Integer> qb = dao.queryBuilder();
            qb.orderBy("src_id", true);
            CloseableIterator<Entry> it = dao.iterator(qb.prepare());

            Map<String, Integer> collabsAlphabetical = new TreeMap<>();
            while (it.hasNext()) {
                Entry e = it.next();
                if (e.srcId <= 99) {
                    if (e.srcId != 95) {
                        res.add(e.srcId);
                    }
                } else {
                    collabsAlphabetical.put(
                            (e.srcNameEn.startsWith("The ") ? e.srcNameEn.substring(4) : e.srcNameEn).toLowerCase(
                                    Locale.ROOT), e.srcId);
                }
            }

            for (Map.Entry<String, Integer> e : collabsAlphabetical.entrySet()) {
                res.add(e.getValue());
            }

            return res;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void createSrcFilterTemplate() {
        try {
            File file = new File("templates/filters_source.phtml");
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IOException("mkdirs failed");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("\t\t\t<option value=\"0\">-</option>\n");

            for (int sourceId : getSortedSourceIDList()) {
                if (sourceId == 99) {
                    writer.write("\t\t\t<optgroup label=\"Collaborations\">\n");
                } else if (sourceId != 95) {
                    writer.write(
                            "\t\t\t<option value=\"" + sourceId + "\">" + getSourceNameEn(sourceId) + "</option>\n");
                }
            }

            writer.write("\t\t\t</optgroup>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DatabaseTable(tableName = "sources")
    public static class Entry {
        @DatabaseField(columnName = "src_id", id = true)
        public int srcId;
        @DatabaseField(columnName = "src_name_en")
        public String srcNameEn;
        @DatabaseField(columnName = "src_name_jp")
        public String srcNameJp;
        @DatabaseField(columnName = "src_short")
        public String srcShort;

        // for ORMLite
        private Entry() {
        }

        private Entry(int srcId) {
            Logger.notify("New source (ID: " + srcId + ") added, needs name.");
            this.srcId = srcId;
            this.srcNameEn = "[UNKNOWN]";
            this.srcNameJp = "[UNKNOWN]";
            this.srcShort = "[UNKNOWN]";
        }
    }
}
