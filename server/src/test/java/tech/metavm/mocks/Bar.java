package tech.metavm.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ValueType;

@EntityType("巴")
public final class Bar extends Entity {
    @EntityField(value = "编号", asTitle = true)
    private final String code;

    public Bar(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}
