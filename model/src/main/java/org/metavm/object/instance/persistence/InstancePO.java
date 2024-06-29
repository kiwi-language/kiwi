package org.metavm.object.instance.persistence;

import org.metavm.entity.Tree;

import java.util.Arrays;
import java.util.Objects;

public class InstancePO {
    private long appId;
    private long id;
    private byte[] data;
    private long version;
    private long syncVersion;
    private long nextNodeId;

    public InstancePO() {
    }

    public InstancePO(long appId,
                      long id,
                      byte[] data,
                      long version,
                      long syncVersion,
                      long nextNodeId
                      ) {
        this.appId = appId;
        this.id = id;
        this.data = data;
        this.version = version;
        this.syncVersion = syncVersion;
        this.nextNodeId = nextNodeId;
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

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSyncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public long getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(long nextNodeId) {
        this.nextNodeId = nextNodeId;
    }

    public VersionPO nextVersion() {
        return new VersionPO(
                appId, id, version + 1
        );
    }

    public void setId(long id) {
        this.id = id;
    }

    public Tree toTree() {
        return new Tree(id, version, nextNodeId, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstancePO that = (InstancePO) o;
        return Objects.equals(appId, that.appId) && id == that.id && Arrays.equals(data, that.data) && Objects.equals(version, that.version) && Objects.equals(syncVersion, that.syncVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, id, Arrays.hashCode(data), version, syncVersion);
    }

    @Override
    public String toString() {
        return "InstancePO{" +
                "appId=" + appId +
                ", id=" + id +
                ", version=" + version +
                ", syncVersion=" + syncVersion +
                '}';
    }

    public InstancePO copy() {
        return new InstancePO(appId, id, data, version, syncVersion, nextNodeId);
    }

}
