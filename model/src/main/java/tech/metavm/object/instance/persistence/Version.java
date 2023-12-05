package tech.metavm.object.instance.persistence;

import java.util.Objects;

public class Version {
    private long id;
    private long version;

    public Version(long id, long version) {
        this.id = id;
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
