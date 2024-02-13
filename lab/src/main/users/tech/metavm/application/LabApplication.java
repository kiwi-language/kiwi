package tech.metavm.application;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("应用")
public class LabApplication {

    @EntityField(value = "名称", asTitle = true)
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
