package org.metavm.manufacturing.material;

import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

@EntityType
public class Supplier {
    private String code;
    @EntityField(asTitle = true)
    private String name;

    public Supplier(String code, String name) {
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

