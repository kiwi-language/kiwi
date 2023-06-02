package tech.metavm.entity;

public class EntityPO implements Identifiable {

    private Long id;

    private Long tenantId;

    private Long version;

    private Long syncVersion;

    public EntityPO(Long id, Long tenantId) {
        this.id = id;
        this.tenantId = tenantId;
        version = 0L;
        syncVersion = 0L;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getSyncVersion() {
        return syncVersion;
    }

    public void setSyncVersion(Long syncVersion) {
        this.syncVersion = syncVersion;
    }
}
