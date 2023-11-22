package tech.metavm.object.instance.persistence;

public class RootInfo {

    private long id;
    private long rootId;
    private long rootVersion;

    public RootInfo(long id, long rootId, long rootVersion) {
        this.id = id;
        this.rootId = rootId;
        this.rootVersion = rootVersion;
    }

    public RootInfo() {
    }

    public long getId() {
        return id;
    }

    public long getRootId() {
        return rootId;
    }

    public void setRootId(long rootId) {
        this.rootId = rootId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRootVersion() {
        return rootVersion;
    }

    public void setRootVersion(long rootVersion) {
        this.rootVersion = rootVersion;
    }

    public Version getVersion() {
        return new Version(rootId, rootVersion);
    }

}
