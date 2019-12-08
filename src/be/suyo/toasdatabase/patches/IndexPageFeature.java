package be.suyo.toasdatabase.patches;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import be.suyo.toasdatabase.units.Unit;
import be.suyo.toasdatabase.utils.DatabaseConnection;

@DatabaseTable(tableName = "index_page_features")
public class IndexPageFeature {
    private static Dao<IndexPageFeature, Integer> dao = DatabaseConnection.getDao(IndexPageFeature.class);

    @DatabaseField(columnName = "idxf_id", id = true)
    public int idxfId = 0;

    @DatabaseField(columnName = "idxf_patch_id", foreign = true)
    public Patch idxfPatch;

    @DatabaseField(columnName = "idxf_title")
    public String idxfTitle;

    @DatabaseField(columnName = "idxf_unit_image")
    public String idxfUnitImage;

    @DatabaseField(columnName = "idxf_bg_image")
    public String idxfBgImage;

    @DatabaseField(columnName = "idxf_unit_image_pos")
    public int idxfUnitImagePos = 75;

    @DatabaseField(columnName = "idxf_bg_image_pos")
    public int idxfBgImagePos = 45;

    @DatabaseField(columnName = "idxf_thumb_units")
    public String idxfThumbUnits;

    public IndexPageFeature() {
    }

    public IndexPageFeature(PatchCategory patchCat, Unit featureUnit) {
        this.idxfPatch = patchCat.pcatPatch;
        this.idxfTitle = patchCat.pcatName.replace("Gacha: ", "");
        this.idxfUnitImage = featureUnit.unitImageMypage;
        this.idxfBgImage = featureUnit.unitImageBackground;

        List<Unit> thumbUnits = new LinkedList<>();
        CloseableIterator<PatchUnit> patchUnits = PatchUnit.getByPCatId(patchCat.pcatId);
        while (patchUnits.hasNext()) {
            Unit u = Unit.get(patchUnits.next().punitUnitId);
            assert u != null;
            if (u.unitRarity == Unit.Rarity.FOUR || u.unitRarity == Unit.Rarity.AWAKENED) {
                thumbUnits.add(u);
            }
        }

        thumbUnits.sort((u1, u2) -> {
            if (u1.unitRarity == u2.unitRarity) {
                return u1.unitId - u2.unitId;
            } else {
                return u2.unitRarity.ordinal() - u1.unitRarity.ordinal();
            }
        });

        StringBuilder sb = new StringBuilder("" + thumbUnits.get(0).unitId);
        thumbUnits.remove(0);
        for (Unit u : thumbUnits) {
            sb.append(",").append(u.unitId);
        }
        this.idxfThumbUnits = sb.toString();
    }

    static public void setIndexPageFeature(int id, PatchCategory patchCat, int featureUnitId) {
        Unit u = Unit.get(featureUnitId);
        assert u != null;
        IndexPageFeature ipf = new IndexPageFeature(patchCat, u);
        ipf.idxfId = id;
        try {
            dao.deleteById(id);
            dao.create(ipf);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
