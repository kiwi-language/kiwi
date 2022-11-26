package tech.metavm.user.persistence;

import tech.metavm.entity.EntityPO;

public class RolePO extends EntityPO {

    private String name;

    public RolePO(Long id, Long tenantId, String name) {
        super(id, tenantId);
        this.name = name;
    }

    public RolePO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
