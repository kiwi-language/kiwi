package org.metavm.object.instance;

import org.metavm.entity.DefContext;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.Mapper;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.instance.core.PrimitiveInstance;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.ReflectionUtils;

import java.util.function.BiConsumer;

public class DefaultObjectInstanceMap implements ObjectInstanceMap {

    private final IEntityContext entityContext;
    private final BiConsumer<Object, DurableInstance> addMapping;

    public DefaultObjectInstanceMap(IEntityContext entityContext, BiConsumer<Object, DurableInstance> addMapping) {
        this.entityContext = entityContext;
        this.addMapping = addMapping;
    }

    private DefContext getDefContext() {
        return entityContext.getDefContext();
    }

    @Override
    public Instance getInstance(Object object) {
        var primitiveInst = Instances.trySerializePrimitive(object, getDefContext()::getType);
        if (primitiveInst != null)
            return primitiveInst;
        else {
            return new InstanceReference(null, () -> entityContext.getInstance(object));
//            if(object instanceof Identifiable identifiable && identifiable.tryGetId() instanceof PhysicalId id)
//                return entityContext.getInstanceContext().createReference(id);
//            else
//                return entityContext.getInstance(object).getReference();
        }
    }

    public <T> T getEntity(Class<T> klass, Instance instance, Mapper<T, ?> mapper) {
        //noinspection unchecked
        klass = (Class<T>) ReflectionUtils.getBoxedClass(klass);
        if (instance instanceof PrimitiveInstance primitiveInstance)
            return klass.cast(Instances.deserializePrimitive(primitiveInstance, klass));
        else if(instance instanceof InstanceReference r) {
            if(r.tryGetId() != null)
                return entityContext.getEntity(klass, r.getId());
            else
                return entityContext.getEntity(klass, r.resolve());
        }
        else
            throw new InternalException("Invalid instance: " + instance);
    }

    @Override
    public Type getType(java.lang.reflect.Type javaType) {
        return entityContext.getDefContext().getType(javaType);
    }

    @Override
    public void addMapping(Object entity, DurableInstance instance) {
        addMapping.accept(entity, instance);
    }

}
