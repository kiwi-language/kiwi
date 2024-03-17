package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.user.rest.dto.RoleDTO;

@EntityType("角色")
public class Role extends Entity {

    @EntityField(value = "名称", asTitle = true)
    private String name;

    public Role() {
    }

    public Role(Long tmpId, String name) {
        setTmpId(tmpId);
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
        try (var serContext = SerializeContext.enter()) {
            return new RoleDTO(
                    serContext.getId(this),
                    getName()
            );
        }
    }


}
