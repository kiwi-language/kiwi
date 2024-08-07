package org.metavm.mocks;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;

@EntityType
public class UpgradeFoo extends Entity {

    private final String name;
    @EntityField(since = 1)
    private final String code;

    public UpgradeFoo(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
