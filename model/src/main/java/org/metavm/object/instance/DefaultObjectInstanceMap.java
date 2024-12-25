package org.metavm.object.instance;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.DefContext;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.Mapper;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeTags;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.ReflectionUtils;

import java.util.function.BiConsumer;

@Slf4j
public class DefaultObjectInstanceMap implements ObjectInstanceMap {

    private final IEntityContext entityContext;
    private final BiConsumer<Object, Instance> addMapping;

    public DefaultObjectInstanceMap(IEntityContext entityContext, BiConsumer<Object, Instance> addMapping) {
        this.entityContext = entityContext;
        this.addMapping = addMapping;
    }

    private DefContext getDefContext() {
        return entityContext.getDefContext();
    }

    @Override
    public Value getInstance(Object object) {
        if(object instanceof Value value)
            return value;
        var primitiveInst = Instances.trySerializePrimitive(object, getDefContext()::getType);
        if (primitiveInst != null)
            return primitiveInst;
        else {
            return new Reference(null, () -> entityContext.getInstance(object));
//            if(object instanceof Identifiable identifiable && identifiable.tryGetId() instanceof PhysicalId id)
//                return entityContext.getInstanceContext().createReference(id);
//            else
//                return entityContext.getInstance(object).getReference();
        }
    }

    public <T> T getEntity(Class<T> klass, Value instance, Mapper<T, ?> mapper) {
        //noinspection unchecked
        klass = (Class<T>) ReflectionUtils.getWrapperClass(klass);
        switch (instance) {
            case NullValue nullValue -> {
                return null;
            }
            case ElementValue elementValue -> {
                return klass.cast(elementValue);
            }
            case PrimitiveValue primitiveValue -> {
                return klass.cast(Instances.deserializePrimitive(primitiveValue, klass));
            }
            case Reference r -> {
                var id = r.tryGetId();
                if (id != null) {
                    if (TypeTags.isSystemTypeTag(id.getTypeTag(entityContext)))
                        return entityContext.getEntity(klass, id);
                    else
                        return klass.cast(instance);
                } else
                    return entityContext.getEntity(klass, r.resolve());
            }
            case null, default -> throw new InternalException("Invalid instance: " + instance.getClass().getName());
        }
    }

    @Override
    public Type getType(java.lang.reflect.Type javaType) {
        return entityContext.getDefContext().getType(javaType);
    }

    @Override
    public void addMapping(Object entity, Instance instance) {
        addMapping.accept(entity, instance);
    }

}
