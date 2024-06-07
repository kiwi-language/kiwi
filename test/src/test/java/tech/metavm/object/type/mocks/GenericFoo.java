package tech.metavm.object.type.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

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
