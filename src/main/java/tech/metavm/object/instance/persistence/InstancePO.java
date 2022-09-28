package tech.metavm.object.instance.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class InstancePO {
    private final long tenantId;
    private Long id;
    private final long modelId;
    private final String title;
    private final Map<String, Object> data;
    private final long version;
    private final long syncVersion;

    public InstancePO(
            long tenantId,
            Long id,
            long modelId,
            String title,
            Map<String, Object> data,
            long version,
            long syncVersion
    ) {
        this.tenantId = tenantId;
        this.id = id;
        this.modelId = modelId;
        this.title = title;
        this.data = data;
        this.version = version;
        this.syncVersion = syncVersion;
    }

    public static InstancePO newInstance(long tenantId, Long id, long modelId, String title, long version, long syncVersion) {
        return new InstancePO(tenantId, id, modelId, title, new HashMap<>(), version, syncVersion);
    }

    public Object get(String column) {
        return data.get(column);
    }

    public int getInt(String column) {
        return (int) data.get(column);
    }

    public String getString(String column) {
        return (String) data.get(column);
    }

    public void put(String columnName, Object object) {
        data.put(columnName, object);
    }

    public long tenantId() {
        return tenantId;
    }

    public Long id() {
        return id;
    }

    public long modelId() {
        return modelId;
    }

    public String title() {
        return title;
    }

    public Map<String, Object> data() {
        return data;
    }

    public long version() {
        return version;
    }

    public long syncVersion() {
        return syncVersion;
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
                this.modelId == that.modelId &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.data, that.data) &&
                this.version == that.version &&
                this.syncVersion == that.syncVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id, modelId, title, data, version, syncVersion);
    }

    @Override
    public String toString() {
        return "InstancePO[" +
                "tenantId=" + tenantId + ", " +
                "objectId=" + id + ", " +
                "modelId=" + modelId + ", " +
                "title=" + title + ", " +
                "data=" + data + ", " +
                "version=" + version + ", " +
                "syncVersion=" + syncVersion + ']';
    }

}
