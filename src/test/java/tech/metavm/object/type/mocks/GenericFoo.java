package tech.metavm.object.type.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

@EntityType(value = "范型测试", compiled = true)
public class GenericFoo<T> extends Entity {

    T value;


    T getValue() {
        return value;
    }

    void setValue(T value) {
        this.value = value;
    }


}
