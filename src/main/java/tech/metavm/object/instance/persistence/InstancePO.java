package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.metavm.entity.Identifiable;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
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
    private @Nullable String title;
    private Map<String, Map<String, @org.jetbrains.annotations.Nullable Object>> data;
    private @Nullable Long parentId;
    private @Nullable Long parentFieldId;
    private Long version;
    private Long syncVersion;

    public InstancePO() {
    }

    public InstancePO(Long tenantId,
                      Long id,
                      Long typeId,
                      @Nullable String title,
                      Map<String, Map<String, @org.jetbrains.annotations.Nullable Object>> data,
                      @Nullable Long parentId,
                      @Nullable Long parentFieldId,
                      Long version,
                      Long syncVersion) {
        this.tenantId = tenantId;
        this.id = id;
        this.typeId = typeId;
        this.title = title;
        this.parentId = parentId;
        this.parentFieldId = parentFieldId;
        this.data = data;
        this.version = version;
        this.syncVersion = syncVersion;
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

    @Nullable
    public Object get(long typeId, String column) {
        return getSubMap(typeId).get(column);
    }

    private Map<String, @org.jetbrains.annotations.Nullable Object> getSubMap(long typeId) {
        return data.computeIfAbsent(NncUtils.toBase64(typeId), k -> new HashMap<>());
    }

    public void set(long typeId, String column, @Nullable Object value) {
        getSubMap(typeId).put(column, value);
    }

    public void put(long typeId, String columnName, @Nullable Object object) {
        getSubMap(typeId).put(columnName, object);
    }

    public Long getId() {
        return id;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public Map<String, Map<String ,@org.jetbrains.annotations.Nullable Object>> getData() {
        return data;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    @Nullable
    public Long getParentFieldId() {
        return parentFieldId;
    }

    public void setParentId(@Nullable Long parentId) {
        this.parentId = parentId;
    }

    public void setParentFieldId(@Nullable Long parentFieldId) {
        this.parentFieldId = parentFieldId;
    }

    public void setData(Map<String, Map<String ,@org.jetbrains.annotations.Nullable Object>> data) {
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
