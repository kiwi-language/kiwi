package org.metavm.mocks;

import org.metavm.entity.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;

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
