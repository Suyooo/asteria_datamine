package be.suyo.toasdatabase.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import be.suyo.toasdatabase.units.Unit;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.logging.Logger;

import static java.util.Map.entry;

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

    private static Map<Integer, int[]> merges =
            Map.ofEntries(entry(1201, new int[]{9986}), entry(1202, new int[]{9987}),
                    entry(1203, new int[]{9015, 9988}), entry(1204, new int[]{9012}), entry(1401, new int[]{9002}),
                    entry(1402, new int[]{9005}), entry(1701, new int[]{1723, 9016}),
                    entry(1710, new int[]{3601, 9013}), entry(2201, new int[]{9003}), entry(2401, new int[]{2409}),
                    entry(2501, new int[]{2521, 2550, 9009}), entry(2502, new int[]{2551}),
                    entry(2503, new int[]{2523}), entry(2505, new int[]{2524}), entry(2801, new int[]{3114}),
                    entry(2802, new int[]{3121, 9004}), entry(2803, new int[]{3115}), entry(2804, new int[]{3116}),
                    entry(2805, new int[]{3117}), entry(2806, new int[]{3118}), entry(2808, new int[]{3120}),
                    entry(2810, new int[]{3122}), entry(2817, new int[]{3119}),
                    entry(3300, new int[]{3301, 3501, 3502, 9014}), entry(3302, new int[]{3505, 3506}),
                    entry(3303, new int[]{9011}), entry(3304, new int[]{9010}), entry(3305, new int[]{3321, 3507}),
                    entry(3307, new int[]{3410}), entry(3308, new int[]{3503, 3504}), entry(3411, new int[]{3412}),
                    entry(3416, new int[]{3417}), entry(3501, new int[]{3502}), entry(3503, new int[]{3504}),
                    entry(3505, new int[]{3506}), entry(3701, new int[]{3702}), entry(9001, new int[]{9008}));

    public static void createCharFilterTemplate() {
        try {
            Map<String, String> combined = new TreeMap<>();

            File file = new File("templates/filters_character.phtml");
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IOException("mkdirs failed");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("\t\t\t<option value=\"0\">-</option>\n");

            String indentHtml = "";
            String indentText = "";

            for (int sourceId : Sources.getSortedSourceIDList()) {
                if (sourceId == 99) {
                    // Collabs begin
                    writer.write("\t\t\t<optgroup label=\"Collaborations\">\n");
                    indentHtml = "\t";
                    indentText = "&nbsp;&nbsp;&nbsp;&nbsp;";
                    continue;
                }

                writer.write(indentHtml + "\t\t\t<optgroup label=\"" + indentText + Sources.getSourceNameEn(sourceId) +
                        "\">\n");

                CloseableIterator<Unit> it = Unit.getCharsForSource(sourceId);
                while (it.hasNext()) {
                    Unit e = it.next();
                    int charId = e.unitId / 10000;

                    // Char IDs to skip (merged with others, should not appear as a seperate filter option)
                    // 1723: Short hair Luke, merged with 1701 (long hair Luke)
                    // 3301: Shepherd Sorey, merged with 3300 (Non-Shepherd Sorey)
                    // 3321: Lailah, got new ID with seiyuu change, merged with 3305 (old ID)
                    // 3502, 3504, 3506: TOZX BAW characters with seperate IDs, see below
                    // 3702: Unmasked Alphen, merged with 3701 (Iron Mask Alphen)
                    // 9008: Purified Tirug, merged with 9001 (Dark Tirug)
                    if (charId == 1723 || charId == 3301 || charId == 3321 || charId == 3502 || charId == 3504 ||
                            charId == 3506 || charId == 3702 || charId == 9008) {
                        continue;
                    }

                    Entry character = dao.queryForId(charId);
                    String name = character.charNameEn;
                    if (!character.charNameEn.equals("Celsius") && character.charSubtitleEn != null) {
                        // Add subtitle (unless it's Celsius who's different characters in different games)
                        name += " [" + character.charSubtitleEn + "]";
                    }

                    writer.write(indentHtml + "\t\t\t\t<option value=\"" + charId);

                    if (merges.containsKey(charId)) {
                        for (int mergedCharId : merges.get(charId)) {
                            writer.write("," + mergedCharId);
                        }
                    }

                    writer.write("\">" + indentText + name + "</option>\n");
                }

                writer.write(indentHtml + "\t\t\t</optgroup>\n");
            }
            writer.write("\t\t\t</optgroup>");
            writer.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
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
