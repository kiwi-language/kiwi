package org.metavm.object.instance;

import org.metavm.entity.Mapper;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public interface ObjectInstanceMap {

    Value getInstance(Object object);

    <T> T getEntity(Class<T> klass, Value instance, @Nullable Mapper<T, ?> mapper);

    Type getType(java.lang.reflect.Type javaType);

    default  <T> T getEntity(Class<T> klass, Value instance) {
        return getEntity(klass, instance, null);
    }

    void addMapping(Object entity, Instance instance);

}
