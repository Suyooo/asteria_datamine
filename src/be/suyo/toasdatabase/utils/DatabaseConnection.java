package be.suyo.toasdatabase.utils;

import java.io.File;
import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import be.suyo.toasdatabase.news.NewsPost;
import be.suyo.toasdatabase.partners.Partner;
import be.suyo.toasdatabase.partners.PartnerMatch;
import be.suyo.toasdatabase.partners.PartnerSchedule;
import be.suyo.toasdatabase.patches.IndexPageFeature;
import be.suyo.toasdatabase.patches.Patch;
import be.suyo.toasdatabase.patches.PatchCategory;
import be.suyo.toasdatabase.patches.PatchUnit;
import be.suyo.toasdatabase.souls.Soul;
import be.suyo.toasdatabase.souls.SoulMatch;
import be.suyo.toasdatabase.souls.SoulSkill;
import be.suyo.toasdatabase.units.Arte;
import be.suyo.toasdatabase.units.BondPotential;
import be.suyo.toasdatabase.units.CoopSkill;
import be.suyo.toasdatabase.units.ExSkill;
import be.suyo.toasdatabase.units.MysticArte;
import be.suyo.toasdatabase.units.MysticArteEx;
import be.suyo.toasdatabase.units.Subquest;
import be.suyo.toasdatabase.units.Unit;

public class DatabaseConnection {
    private static ConnectionSource connectionSource;
    static {
        if (!(new File("database.db").exists())) {
            throw new DatabaseException("database.db does not exist");
        }
        
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
        try {
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:database.db");

            TableUtils.createTableIfNotExists(connectionSource, Global.KVPair.class);
            TableUtils.createTableIfNotExists(connectionSource, Characters.Entry.class);
            TableUtils.createTableIfNotExists(connectionSource, Sources.Entry.class);
            TableUtils.createTableIfNotExists(connectionSource, Subtitle.class);

            TableUtils.createTableIfNotExists(connectionSource, NewsPost.class);

            TableUtils.createTableIfNotExists(connectionSource, Arte.class);
            TableUtils.createTableIfNotExists(connectionSource, BondPotential.class);
            TableUtils.createTableIfNotExists(connectionSource, CoopSkill.class);
            TableUtils.createTableIfNotExists(connectionSource, ExSkill.class);
            TableUtils.createTableIfNotExists(connectionSource, MysticArte.class);
            TableUtils.createTableIfNotExists(connectionSource, MysticArteEx.class);
            TableUtils.createTableIfNotExists(connectionSource, Soul.class);
            TableUtils.createTableIfNotExists(connectionSource, SoulMatch.class);
            TableUtils.createTableIfNotExists(connectionSource, SoulSkill.class);
            TableUtils.createTableIfNotExists(connectionSource, Subquest.class);
            TableUtils.createTableIfNotExists(connectionSource, Unit.class);

            TableUtils.createTableIfNotExists(connectionSource, Partner.class);
            TableUtils.createTableIfNotExists(connectionSource, PartnerMatch.class);
            TableUtils.createTableIfNotExists(connectionSource, PartnerSchedule.class);

            TableUtils.createTableIfNotExists(connectionSource, Patch.class);
            TableUtils.createTableIfNotExists(connectionSource, PatchCategory.class);
            TableUtils.createTableIfNotExists(connectionSource, PatchUnit.class);
            TableUtils.createTableIfNotExists(connectionSource, IndexPageFeature.class);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static <E, K> Dao<E, K> getDao(Class<E> c) {
        try {
            return DaoManager.createDao(connectionSource, c);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
