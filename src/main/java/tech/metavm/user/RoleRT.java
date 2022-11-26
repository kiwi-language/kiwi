package tech.metavm.user;

import tech.metavm.entity.*;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.IdConstants;
import tech.metavm.user.persistence.RolePO;
import tech.metavm.user.rest.dto.RoleDTO;

import java.util.List;

import static tech.metavm.object.meta.IdConstants.ROLE;
import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("角色")
public class RoleRT extends Entity {

    private String name;

//    public RoleRT(RolePO rolePO, EntityContext context) {
//        super(rolePO.getId(), context);
//    }

//    public RoleRT(RoleDTO roleDTO, EntityContext context) {
//        super(context);
//        setName(roleDTO.name());
//    }

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
