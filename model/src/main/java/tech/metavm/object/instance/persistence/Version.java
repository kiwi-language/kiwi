package tech.metavm.object.instance.persistence;

import java.util.Objects;

public class Version {
    private long id;
    private int typeTag;
    private long typeId;
    private long version;

    public Version(long id, int typeTag, long typeId, long version) {
        this.id = id;
        this.typeTag = typeTag;
        this.typeId = typeId;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTypeTag() {
        return typeTag;
    }

    public void setTypeTag(int typeTag) {
        this.typeTag = typeTag;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Version) obj;
        return this.id == that.id &&
                this.version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "Version[" +
                "id=" + id + ", " +
                "version=" + version + ']';
    }

}
