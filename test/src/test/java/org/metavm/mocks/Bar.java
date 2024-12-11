package org.metavm.mocks;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

@Entity
public class Bar extends org.metavm.entity.Entity {
    @EntityField(asTitle = true)
    private final String code;

    public Bar(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}
