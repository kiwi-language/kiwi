package org.metavm.object.instance;

import org.metavm.entity.Mapper;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public interface ObjectInstanceMap {

    Instance getInstance(Object object);

    <T> T getEntity(Class<T> klass, Instance instance, @Nullable Mapper<T, ?> mapper);

    Type getType(java.lang.reflect.Type javaType);

    default Klass getClassType(DurableInstance instance) {
        return getEntity(Klass.class, instance);
    }

    default Type getType(DurableInstance instance) {
        return getEntity(Type.class, instance);
    }

    default Field getField(DurableInstance instance) {
        return getEntity(Field.class, instance);
    }

    default  <T> T getEntity(Class<T> klass, Instance instance) {
        return getEntity(klass, instance, null);
    }

}
