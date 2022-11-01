package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;

public class InstanceEntityFactory {

    public static <T extends InstanceEntity> T create(Instance instance, Class<T> entityType) {
        EntityContext context = instance.getContext().getEntityContext();
        Type type = context.getType(instance.getType().getId());
        Class<? extends InstanceEntity> actualEntityType = InstanceEntityType.getByType(type).getEntityType();
        if(!entityType.isAssignableFrom(actualEntityType)) {
            throw new InternalException("Entity type mismatch. " +
                    "Instance id: " + instance.getId() +
                    ", expected entity type: " + entityType.getName() +
                    ", actual entity type: " + actualEntityType.getName());
        }
        Constructor<T> constructor
                = (Constructor) ReflectUtils.getConstructor(actualEntityType, Instance.class);
        return ReflectUtils.newInstance(constructor, instance);
    }

}
