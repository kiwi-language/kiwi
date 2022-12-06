package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.metavm.entity.Identifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = InstancePO.class),
                @JsonSubTypes.Type(name = "2", value = InstanceArrayPO.class)
        }
)
public class InstancePO implements Identifiable {
    private Long tenantId;
    private Long id;
    private Long typeId;
    private String title;
    private Map<String, Object> data;
    private Long version;
    private Long syncVersion;

    public InstancePO(Long tenantId,
                      Long id,
                      Long typeId,
                      String title,
                      Map<String, Object> data,
                      Long version,
                      Long syncVersion) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstancePO that = (InstancePO) o;
        return Objects.equals(tenantId, that.tenantId) && Objects.equals(id, that.id) && Objects.equals(typeId, that.typeId) && Objects.equals(title, that.title) && Objects.equals(data, that.data) && Objects.equals(version, that.version) && Objects.equals(syncVersion, that.syncVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id, typeId, title, data, version, syncVersion);
    }

    @Override
    public String toString() {
        return "InstancePO{" +
                "tenantId=" + tenantId +
                ", id=" + id +
                ", typeId=" + typeId +
                ", title='" + title + '\'' +
                ", data=" + data +
                ", version=" + version +
                ", syncVersion=" + syncVersion +
                '}';
    }
}
