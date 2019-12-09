package be.suyo.toasdatabase.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

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

    public static void createSrcFilterTemplate() {
        try {
            Map<String, String> combined = new TreeMap<>();
            CloseableIterator<Entry> it = getIterator();
            assert it != null;
            while (it.hasNext()) {
                Entry e = it.next();
                String s = "";
                if (combined.containsKey(e.srcNameEn)) {
                    s = combined.get(e.srcNameEn) + ",";
                }
                s += e.srcId;
                combined.put(e.srcNameEn, s);
            }

            Map<String, String> ordered = new TreeMap<>();
            for (Map.Entry<String, String> e : combined.entrySet()) {
                ordered.put(e.getValue(), e.getKey());
            }

            File file = new File("templates/filters_source.phtml");
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IOException("mkdirs failed");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("\t\t\t<option value=\"0\">-</option>\n");
            for (Map.Entry<String, String> e : ordered.entrySet()) {
                if (e.getKey().length() <= 2) {
                    if (e.getKey().equals("99")) {
                        writer.write("\t\t\t<optgroup label=\"Collaborations\">\n");
                    } else {
                        writer.write("\t\t\t<option value=\"" + e.getKey() + "\">" + e.getValue() + "</option>\n");
                    }
                } else {
                    writer.write("\t\t\t\t<option value=\"" + e.getKey() + "\">" + e.getValue() + "</option>\n");
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
