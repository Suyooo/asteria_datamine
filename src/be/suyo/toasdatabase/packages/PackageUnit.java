package be.suyo.toasdatabase.packages;

public class PackageUnit {
    public int unitId;
    public String shortName;
    public boolean isEvolved;

    PackageUnit(int unitId, String shortName, boolean isEvolved) {
        this.unitId = unitId;
        this.shortName = shortName;
        this.isEvolved = isEvolved;
    }
}
