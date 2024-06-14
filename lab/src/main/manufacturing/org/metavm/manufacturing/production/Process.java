package org.metavm.manufacturing.production;

import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

@EntityType
public class Process {
    @EntityField(asTitle = true)
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
