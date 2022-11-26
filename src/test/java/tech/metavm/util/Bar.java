package tech.metavm.util;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("Bar")
public class Bar extends Entity {

    @EntityField("编号")
    private final String code;

    public Bar(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
