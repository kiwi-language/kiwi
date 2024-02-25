package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("工序")
public class Process {
    @EntityField(value = "名称", asTitle = true)
    private String name;

    public Process(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
