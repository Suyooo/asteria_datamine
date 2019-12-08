package be.suyo.toasdatabase.utils;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class Global {
    private static Dao<KVPair, String> dao = DatabaseConnection.getDao(KVPair.class);
    private static HashMap<String, KVPair> kvPairSingletons = new HashMap<>();

    public static KVPair getKVPair(String key) {
        KVPair kvp = kvPairSingletons.get(key);
        if (kvp == null) {
            try {
                kvp = dao.queryForId(key);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }

            if (kvp == null) {
                throw new DatabaseException(key + " not found in globals table");
            }
            kvPairSingletons.put(key, kvp);
        }
        return kvp;
    }

    public static String getValue(String key) {
        return getKVPair(key).value;
    }

    public static void setValue(String key, String value) {
        getKVPair(key).setValueAndUpdate(value);
    }

    public static Long makeActionURLHash(String transferPageId, String params) {
        int hash = (transferPageId + "___" + params).hashCode();
        return 10_000_000_000L + (((long) hash) & 0x00000000FFFFFFFFL);
    }

    private static boolean isFullWidth(String s) {
        try {
            return s.getBytes("MS932").length == 2;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String pad(String s, int len) {
        int width = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isFullWidth(s.substring(i, i + 1))) {
                width += 2;
            } else {
                width++;
            }
        }
        StringBuilder sBuilder = new StringBuilder(s);
        while (width < len) {
            sBuilder.append(" ");
            width++;
        }
        s = sBuilder.toString();
        return s;
    }

    public static String pad(int i, int len) {
        return pad("" + i, len);
    }

    @DatabaseTable(tableName = "global")
    public static class KVPair {
        @DatabaseField(columnName = "key", id = true)
        public String key;
        @DatabaseField(columnName = "value")
        public String value;

        public KVPair() {
        }

        public void setValueAndUpdate(String value) {
            try {
                this.value = value;
                dao.update(this);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}