package tech.metavm.application;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType
public class LabApplication {

    @EntityField(asTitle = true)
    private String name;

    public LabApplication(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
