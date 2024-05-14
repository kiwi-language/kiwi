package tech.metavm.object.instance;

import tech.metavm.entity.Mapper;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;

public interface ObjectInstanceMap {

    Instance getInstance(Object object);

    <T> T getEntity(Class<T> klass, Instance instance, @Nullable Mapper<T, ?> mapper);

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
