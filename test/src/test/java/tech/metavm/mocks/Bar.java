package tech.metavm.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

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
