package tech.metavm.object.instance.persistence;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.EncodingUtils;

import java.util.Arrays;
import java.util.Objects;

public class InstancePO {
    private long appId;
    private byte[] id;
    private byte[] typeId;
    private String title;
    private byte[] data;
    private byte[] parentId;
    private byte[] parentFieldId;
    private byte[] rootId;
    private long version;
    private long syncVersion;

    public InstancePO() {
    }

    public InstancePO(long appId,
                      byte[] id,
                      byte[] typeId,
                      String title,
                      byte[] data,
                      byte[] parentId,
                      byte[] parentFieldId,
                      byte[] rootId,
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

    public byte[] getId() {
        return id;
    }

    public byte[] getTypeId() {
        return typeId;
    }

    public void setTypeId(byte[] typeId) {
        this.typeId = typeId;
    }

    public Id getInstanceId() {
        return Id.fromBytes(id);
    }

    public byte[] getData() {
        return data;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public byte[] getParentId() {
        return parentId;
    }

    public byte[] getParentFieldId() {
        return parentFieldId;
    }

    public void setParentId(byte[] parentId) {
        this.parentId = parentId;
    }

    public void setParentFieldId(byte[] parentFieldId) {
        this.parentFieldId = parentFieldId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getRootId() {
        return rootId;
    }

    public Id getRootInstanceId() {
        return Id.fromBytes(getRootId());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRootId(byte[] rootId) {
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

    public void setId(byte[] id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstancePO that = (InstancePO) o;
        return Objects.equals(appId, that.appId) && Arrays.equals(id, that.id) && Arrays.equals(data, that.data) && Objects.equals(version, that.version) && Objects.equals(syncVersion, that.syncVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, Arrays.hashCode(id), Arrays.hashCode(data), version, syncVersion);
    }

    @Override
    public String toString() {
        return "InstancePO{" +
                "appId=" + appId +
                ", id=" + EncodingUtils.bytesToHex(id) +
                ", version=" + version +
                ", syncVersion=" + syncVersion +
                '}';
    }

    public InstancePO copy() {
        return new InstancePO(appId, id, typeId, title, data, parentId, parentFieldId, rootId, version, syncVersion);
    }

}
