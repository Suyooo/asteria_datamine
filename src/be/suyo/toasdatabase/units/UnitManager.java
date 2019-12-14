package be.suyo.toasdatabase.units;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.json.JSONObject;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.tasks.ShutdownManager;
import be.suyo.toasdatabase.utils.Characters;
import be.suyo.toasdatabase.utils.DownloadUtils;
import be.suyo.toasdatabase.utils.Multithread;
import be.suyo.toasdatabase.utils.Sources;

public class UnitManager {
    public static Set<Future<byte[]>> futureSet;

    public static void main(String[] args) {
        System.out.println(downloadUnit(14030130));
        ShutdownManager.shutdown();
    }

    public static Unit downloadUnit(int unit_id) {
        futureSet = new HashSet<>();
        Unit u = updateUnit(unit_id);
        for (Future<byte[]> f : futureSet) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new UnitException("Download failed for unit ID " + unit_id, e.getCause());
            }
        }
        Logger.currentLogger.doPrintln(
                "Downloaded ID " + unit_id + ": " + Characters.getFullNameEn(unit_id / 10000) + " (" +
                        Sources.getSourceNameEn(unit_id / 1000000) + ")");
        return u;
    }

    static Unit updateUnit(int unit_id) {
        Map<String, String> params = new HashMap<>();
        params.put("unit_master_id", "" + unit_id);
        JSONObject json = DownloadUtils.downloadAndDecryptJsonFromUrl("unit/view_max_status", params);
        json.remove("user");

        File file = new File("assets/json/unit/" + unit_id + ".json");
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new UnitException("mkdirs failed");
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(json.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Saving original JSON failed, exception follows. Continuing with database entry.");
            e.printStackTrace();
        }

        if (!json.has("unit")) {
            throw new UnitException("Invalid Unit ID: " + unit_id);
        }

        Unit unit = Unit.get(json.getJSONObject("unit").getInt("unit_id"));
        if (unit == null) {
            unit = new Unit();
        }
        unit.update(json);
        return unit;
    }

    static Arte updateArte(JSONObject json) {
        Arte arte = Arte.get(json.getInt("id"));
        if (arte == null) {
            arte = new Arte();
        }
        arte.update(json);
        return arte;
    }

    static BondPotential updateBondPotential(JSONObject json) {
        BondPotential bpot = BondPotential.get(json.getInt("id"));
        if (bpot == null) {
            bpot = new BondPotential();
        }
        bpot.update(json);
        return bpot;
    }

    static MysticArte updateMysticArte(JSONObject json) {
        MysticArte ma = MysticArte.get(json.getInt("id"));
        if (ma == null) {
            ma = new MysticArte();
        }
        ma.update(json);
        return ma;
    }

    static MysticArteEx updateMysticArteEx(JSONObject json) {
        MysticArteEx maex = MysticArteEx.get(json.getInt("id"));
        if (maex == null) {
            maex = new MysticArteEx();
        }
        maex.update(json);
        return maex;
    }

    static ExSkill updateExSkill(JSONObject json) {
        ExSkill ex = ExSkill.get(json.getInt("id"));
        if (ex == null) {
            ex = new ExSkill();
        }
        ex.update(json);
        return ex;
    }

    static CoopSkill updateCoopSkill(JSONObject json) {
        CoopSkill coop = CoopSkill.get(json.getInt("id"));
        if (coop == null) {
            coop = new CoopSkill();
        }
        coop.update(json);
        return coop;
    }

    static void updateSubquest(JSONObject json, Unit unit) {
        Subquest subq = Subquest.get(json.getInt("id"));
        if (subq == null) {
            subq = new Subquest();
        }
        subq.update(json, unit);
    }

    static String downloadResource(String path) {
        if (path.equals("") || path.equals(".ogg")) {
            return null;
        }
        futureSet.add(Multithread.downloadAndDecryptFileFromResourceUrlAsync(path, true));
        return path;
    }
}