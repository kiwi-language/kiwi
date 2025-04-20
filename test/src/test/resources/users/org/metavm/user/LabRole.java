package org.metavm.user;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

@Entity
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
