package tech.metavm.user;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.InstanceEntity;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.user.rest.dto.RoleDTO;

import java.util.List;

import static tech.metavm.object.meta.StdTypeConstants.ROLE;

public class RoleRT extends InstanceEntity {

    private String name;

    public RoleRT(Instance instance) {
        super(instance);
        setName(instance.getString(ROLE.FID_NAME));
    }

    public RoleRT(RoleDTO roleDTO, EntityContext context) {
        super(context.getRoleType(), List.of(
                InstanceFieldDTO.valueOf(ROLE.FID_NAME, roleDTO.name())
        ));
        setName(roleDTO.name());
    }

    public void update(RoleDTO roleDTO) {
        setName(roleDTO.name());
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleDTO toDTO() {
        return new RoleDTO(
                id,
                name
        );
    }

    @Override
    protected InstanceDTO toInstanceDTO() {
        return InstanceDTO.valueOf(
                id,
                type.getId(),
                name,
                List.of(
                        InstanceFieldDTO.valueOf(ROLE.FID_NAME, name)
                )
        );
    }

}
