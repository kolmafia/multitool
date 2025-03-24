package us.kolmafia.multitool;

public class VersionData {
    private final int version;
    private final boolean modified;

    public VersionData(int ver, boolean mod) {
        this.version = ver;
        this.modified = mod;
    }

    public int getVersion() {
        return version;
    }

    public boolean isModified() {
        return modified;
    }
}
