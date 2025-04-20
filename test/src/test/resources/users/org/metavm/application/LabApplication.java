package org.metavm.application;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

@Entity(searchable = true)
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
