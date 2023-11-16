package tech.metavm.object.type.persistence;

import tech.metavm.util.ContextUtil;

public class IdentityPO {

    public static IdentityPO of(long tenantId, long id) {
        return new IdentityPO(tenantId, id);
    }

    public static IdentityPO of(long id) {
        return new IdentityPO(ContextUtil.getTenantId(), id);
    }

    private long tenantId;
    private long id;

    public IdentityPO() {
    }

    public IdentityPO(long tenantId, long id) {
        this.tenantId = tenantId;
        this.id = id;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
