package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.user.persistence.RolePO;
import tech.metavm.user.rest.dto.RoleDTO;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("角色")
public class RoleRT extends Entity {

    @EntityField(value = "名称", asTitle = true)
    private String name;

    public RoleRT() {
    }

    public RoleRT(String name) {
        this.name = name;
    }

    public void update(RoleDTO roleDTO) {
        setName(roleDTO.name());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RoleDTO toRoleDTO() {
        return new RoleDTO(
                id,
                getName()
        );
    }

    public RolePO toPO() {
        return new RolePO(
                getId(),
                getTenantId(),
                name
        );
    }


}
