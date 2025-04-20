package org.metavm.manufacturing.material;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

@Entity
public class Client {
    private String code;
    @EntityField(asTitle = true)
    private String name;

    public Client(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
