package org.metavm.object.type.mocks;

import org.metavm.entity.Entity;
import org.metavm.api.EntityType;

@EntityType(compiled = true)
public class GenericFoo<T> extends Entity {

    T value;


    T getValue() {
        return value;
    }

    void setValue(T value) {
        this.value = value;
    }


}
