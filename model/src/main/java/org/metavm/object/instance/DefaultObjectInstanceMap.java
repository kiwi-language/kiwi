package org.metavm.object.instance;

import org.metavm.entity.DefContext;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.Mapper;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.PrimitiveInstance;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.ReflectionUtils;

public class DefaultObjectInstanceMap implements ObjectInstanceMap {

    private final IEntityContext entityContext;

    public DefaultObjectInstanceMap(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    private DefContext getDefContext() {
        return entityContext.getDefContext();
    }

    @Override
    public Instance getInstance(Object object) {
        var primitiveInst = Instances.trySerializePrimitive(object, getDefContext()::getType);
        if (primitiveInst != null)
            return primitiveInst;
        else
            return entityContext.getInstance(object);
    }

    public <T> T getEntity(Class<T> klass, Instance instance, Mapper<T, ?> mapper) {
        //noinspection unchecked
        klass = (Class<T>) ReflectionUtils.getBoxedClass(klass);
        if (instance instanceof PrimitiveInstance primitiveInstance)
            return klass.cast(Instances.deserializePrimitive(primitiveInstance, klass));
        else if(instance instanceof DurableInstance d)
            return entityContext.getEntity(klass, d, mapper);
        else
            throw new InternalException("Invalid instance: " + instance);
    }

    @Override
    public Type getType(java.lang.reflect.Type javaType) {
        return entityContext.getDefContext().getType(javaType);
    }

}
