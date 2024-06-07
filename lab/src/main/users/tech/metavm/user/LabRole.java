package tech.metavm.user;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType
public class LabRole {

    @EntityField(asTitle = true)
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
