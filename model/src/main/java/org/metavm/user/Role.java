package org.metavm.user;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.SerializeContext;
import org.metavm.user.rest.dto.RoleDTO;

@EntityType(searchable = true)
public class Role extends Entity {

    @EntityField(asTitle = true)
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
                    serContext.getStringId(this),
                    getName()
            );
        }
    }


}
