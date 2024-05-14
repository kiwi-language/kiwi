package tech.metavm.object.instance;

import tech.metavm.entity.DefContext;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.Mapper;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectionUtils;

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

}
