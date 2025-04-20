package org.metavm.manufacturing.production;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

@Entity
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
