package tech.metavm.object.instance.persistence;

import tech.metavm.entity.Identifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InstancePO implements Identifiable {
    private Long tenantId;
    private Long id;
    private Long typeId;
    private String title;
    private Map<String, Object> data;
    private Long version;
    private Long syncVersion;

    public InstancePO(
            Long tenantId,
            Long id,
            Long typeId,
            String title,
            Map<String, Object> data,
            Long version,
            Long syncVersion
    ) {
        this.tenantId = tenantId;
        this.id = id;
        this.typeId = typeId;
        this.title = title;
        this.data = data;
        this.version = version;
        this.syncVersion = syncVersion;
    }

    public InstancePO() {
    }

    public static InstancePO newInstance(long tenantId, Long id, long modelId, String title, long version, long syncVersion) {
        return new InstancePO(tenantId, id, modelId, title, new HashMap<>(), version, syncVersion);
    }

    public Long getSyncVersion() {
        return syncVersion;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getVersion() {
        return version;
    }

    public Long getTypeId() {
        return typeId;
    }

    public Object get(String column) {
        return data.get(column);
    }

    public void put(String columnName, Object object) {
        data.put(columnName, object);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setSyncVersion(Long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public VersionPO nextVersion() {
        return new VersionPO(
                tenantId, id, version + 1
        );
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstancePO) obj;
        return this.tenantId == that.tenantId &&
                Objects.equals(this.id, that.id) &&
                this.typeId == that.typeId &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.data, that.data) &&
                this.version == that.version &&
                this.syncVersion == that.syncVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id, typeId, title, data, version, syncVersion);
    }

    @Override
    public String toString() {
        return "InstancePO[" +
                "tenantId=" + tenantId + ", " +
                "objectId=" + id + ", " +
                "modelId=" + typeId + ", " +
                "title=" + title + ", " +
                "data=" + data + ", " +
                "version=" + version + ", " +
                "syncVersion=" + syncVersion + ']';
    }

}
