package tech.metavm.user;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("角色")
public class LabRole {

    @EntityField(value = "名称", asTitle = true)
    private String name;

    public LabRole() {
    }

    public LabRole(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}
