package tech.metavm.object.instance.persistence;

import java.util.Arrays;
import java.util.Objects;

public class InstancePO {
    private long appId;
    private long id;
    private String title;
    private long typeId;
    private byte[] data;
    private long parentId;
    private long parentFieldId;
    private long rootId;
    private long version;
    private long syncVersion;

    public InstancePO() {
    }

    public InstancePO(long appId,
                      long id,
                      String title,
                      long typeId,
                      byte[] data,
                      long parentId,
                      long parentFieldId,
                      long rootId,
                      long version,
                      long syncVersion) {
        this.appId = appId;
        this.id = id;
        this.typeId = typeId;
        this.rootId = rootId;
        this.parentId = parentId;
        this.parentFieldId = parentFieldId;
        this.data = data;
        this.version = version;
        this.syncVersion = syncVersion;
    }

    public long getSyncVersion() {
        return syncVersion;
    }

    public long getAppId() {
        return appId;
    }

    public long getVersion() {
        return version;
    }

    public long getTypeId() {
        return typeId;
    }

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public long getParentId() {
        return parentId;
    }

    public long getParentFieldId() {
        return parentFieldId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public void setParentFieldId(long parentFieldId) {
        this.parentFieldId = parentFieldId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getRootId() {
        return rootId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRootId(long rootId) {
        this.rootId = rootId;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSyncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public VersionPO nextVersion() {
        return new VersionPO(
                appId, id, version + 1
        );
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstancePO that = (InstancePO) o;
        return Objects.equals(appId, that.appId) && Objects.equals(id, that.id) && Objects.equals(typeId, that.typeId) && Arrays.equals(data, that.data) && Objects.equals(version, that.version) && Objects.equals(syncVersion, that.syncVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, id, typeId, Arrays.hashCode(data), version, syncVersion);
    }

    @Override
    public String toString() {
        return "InstancePO{" +
                "appId=" + appId +
                ", id=" + id +
                ", typeId=" + typeId +
                ", version=" + version +
                ", syncVersion=" + syncVersion +
                '}';
    }

    public InstancePO copy() {
        return new InstancePO(appId, id, title, typeId, data, parentId, parentFieldId, rootId, version, syncVersion);
    }

}
