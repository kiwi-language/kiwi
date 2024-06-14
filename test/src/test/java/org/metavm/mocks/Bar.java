package org.metavm.mocks;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

@EntityType
public class Bar extends Entity {
    @EntityField(asTitle = true)
    private final String code;

    public Bar(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}
