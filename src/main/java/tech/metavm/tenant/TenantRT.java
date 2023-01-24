package tech.metavm.tenant;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("租户")
public class TenantRT extends Entity {

    @EntityField("名称")
    private String name;

    public TenantRT(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
