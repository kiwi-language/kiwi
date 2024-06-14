package org.metavm.application;

import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

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
