package org.metavm.object.instance;

import org.metavm.entity.Mapper;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public interface ObjectInstanceMap {

    Instance getInstance(Object object);

    <T> T getEntity(Class<T> klass, Instance instance, @Nullable Mapper<T, ?> mapper);

    Type getType(java.lang.reflect.Type javaType);

    default  <T> T getEntity(Class<T> klass, Instance instance) {
        return getEntity(klass, instance, null);
    }

    void addMapping(Object entity, DurableInstance instance);

}
