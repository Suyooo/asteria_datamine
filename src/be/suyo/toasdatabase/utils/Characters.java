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

public class Characters {
    private static Dao<Entry, Integer> dao = DatabaseConnection.getDao(Entry.class);

    private static PreparedQuery<Entry> queryOrderById;

    static {
        try {
            QueryBuilder<Entry, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy("char_id", true);
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

    public static void save(Entry c) {
        try {
            dao.createOrUpdate(c);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static String getNameEn(int charId) {
        try {
            Entry e = dao.queryForId(charId);
            if (e == null) {
                e = new Entry(charId);
                dao.create(e);
            }
            return e.charNameEn;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static String getFullNameEn(int charId) {
        try {
            Entry e = dao.queryForId(charId);
            if (e == null) {
                e = new Entry(charId);
                dao.create(e);
            }
            return e.getFullNameEn();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void createCharFilterTemplate() {
        try {
            Map<String, String> combined = new TreeMap<>();
            CloseableIterator<Entry> it = getIterator();
            assert it != null;
            while (it.hasNext()) {
                Entry e = it.next();
                String s = "";

                // Exception for GBF 2018 Collab to order it next to GBF 2017 Collab
                if (e.charId / 10 == 996) {
                    s = "9919,#,";
                }

                if (combined.containsKey(e.charNameEn)) {
                    s = combined.get(e.charNameEn) + ",";

                    // Exception for same chars in same game with different IDs
                    // These should not appear as a seperate filter option
                    // 1723: Short hair Luke, merged with 1701 (long hair Luke)
                    // 3301: Sorey, merged with 3300 (Non-Shepherd Sorey)
                    // 3321: Lailah, got new ID with new seiyuu, merged with 3305 (old ID)
                    // 3502, 3504, 3506: see below
                    // 9008: Purified Tirug, merged with 9001 (dark Tirug)
                    if (e.charId != 1723 && e.charId != 3301 && e.charId != 3321 && e.charId != 3502 &&
                            e.charId != 3504 && e.charId != 3506 && e.charId != 9008) {
                        String subname = e.charNameEn;
                        // Exception for different characters with same name (see below)
                        if (!e.charNameEn.equals("Celsius")) {
                            if (e.charSubtitleEn != null) {
                                subname += " [" + e.charSubtitleEn + "]";
                            }
                        }

                        while (combined.containsKey(subname)) {
                            subname += "|";
                        }

                        // Exceptions for TOZX because for some reason the awakenend
                        // versions of their 6*s count as seperate characters
                        // 3501 <-> 3502 (Sorey), 3503 <-> 3504 (Rose), 3505 <-> 3506 (Alisha)
                        if (e.charId == 3501 || e.charId == 3503 || e.charId == 3505) {
                            combined.put(subname, e.charId + "," + (e.charId + 1));
                        } else {
                            combined.put(subname, "" + e.charId);
                            if (e.charNameEn.equals("Celsius")) {
                                // do not extend TOE Celsius list with the ID of TOX Celsius
                                continue;
                            }
                        }
                    }
                }
                s += e.charId;
                combined.put(e.charNameEn, s);
            }

            Map<String, String> ordered = new TreeMap<>();
            for (Map.Entry<String, String> e : combined.entrySet()) {
                ordered.put(e.getValue(), e.getKey());
            }

            File file = new File("templates/filters_character.phtml");
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("mkdirs failed");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("\t\t\t<option value=\"0\">-</option>\n");
            int lastgame = 0;
            for (Map.Entry<String, String> e : ordered.entrySet()) {
                int commaPos = e.getKey().indexOf(",");
                int thisGame = Integer.parseInt((commaPos == -1) ? e.getKey() : e.getKey().substring(0, commaPos)) / 10;
                if (thisGame < 990) {
                    thisGame /= 10;
                }
                if (thisGame != lastgame) {
                    if (lastgame != 0) {
                        writer.write("\t\t\t</optgroup>\n");
                    }
                    writer.write("\t\t\t<optgroup label=\"" + Sources.getSourceNameEn(thisGame) +
                            ((thisGame >= 100) ? " (Collaboration)" : "") + "\">\n");
                    lastgame = thisGame;
                }

                String optionValues = e.getKey();
                // Exception for GBF 2018 Collab to order it next to GBF 2017 Collab
                if (optionValues.startsWith("9919,#,")) {
                    optionValues = optionValues.substring(7);
                }
                String optionName = e.getValue();
                while (optionName.endsWith("|")) {
                    optionName = optionName.substring(0, optionName.length() - 1);
                }
                writer.write("\t\t\t\t<option value=\"" + optionValues + "\">" + optionName + "</option>\n");
            }
            writer.write("\t\t\t</optgroup>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DatabaseTable(tableName = "characters")
    public static class Entry {
        @DatabaseField(columnName = "char_id", id = true)
        public int charId;
        @DatabaseField(columnName = "char_name_en")
        public String charNameEn;
        @DatabaseField(columnName = "char_subtitle_en")
        public String charSubtitleEn;
        @DatabaseField(columnName = "char_short_en")
        public String charShortEn;
        @DatabaseField(columnName = "char_name_jp")
        public String charNameJp;
        @DatabaseField(columnName = "char_subtitle_jp")
        public String charSubtitleJp;
        @DatabaseField(columnName = "char_short_jp")
        public String charShortJp;

        // for ORMLite
        private Entry() {
        }

        private Entry(int charId) {
            Logger.notify("New character (ID: " + charId + ") added, needs name.");
            this.charId = charId;
            this.charNameEn = "[UNKNOWN]";
            this.charShortEn = "[UNKNOWN]";
            this.charNameJp = "[UNKNOWN]";
            this.charShortJp = "[UNKNOWN]";
            this.charSubtitleEn = this.charSubtitleJp = null;
        }

        public String getFullNameEn() {
            return this.charNameEn + ((this.charSubtitleEn != null) ? " [" + this.charSubtitleEn + "]" : "");
        }

        public String getFullNameJp() {
            return this.charNameJp + ((this.charSubtitleJp != null) ? " [" + this.charSubtitleJp + "]" : "");
        }
    }
}
