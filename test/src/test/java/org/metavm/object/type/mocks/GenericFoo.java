package org.metavm.object.type.mocks;

import org.metavm.api.Entity;

@Entity(compiled = true)
public class GenericFoo<T> extends org.metavm.entity.Entity {

    T value;


    T getValue() {
        return value;
    }

    void setValue(T value) {
        this.value = value;
    }


}
