package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;

public abstract class ModelDef<T, I extends Instance> {

    private final Class<T> entityType;
    private final java.lang.reflect.Type genericType;
    private final Class<I> instanceType;

    protected ModelDef(Class<T> entityType, Class<I> instanceType) {
        this(entityType, entityType, instanceType);
    }

    protected ModelDef(TypeReference<T> typeReference, Class<I> instanceType) {
        this(typeReference.getType(), typeReference.getGenericType(), instanceType);
    }

    private ModelDef(Class<T> entityType, java.lang.reflect.Type genericType, Class<I> instanceType) {
        this.entityType = entityType;
        this.genericType = genericType;
        this.instanceType = instanceType;
    }

    public abstract Type getType();

    public abstract T newModel(I instance, ModelMap modelMap);

    public T newModelHelper(IInstance instance, ModelMap modelMap) {
        Instance realInstance = EntityContext.getRealInstance(instance);
        return newModel(instanceType.cast(realInstance), modelMap);
    }

    public abstract void updateModel(T model, I instance, ModelMap modelMap);

    public final I newInstanceHelper(Object object, InstanceMap instanceMap) {
        return newInstance(entityType.cast(object), instanceMap);
    }

    public abstract I newInstance(T model, InstanceMap instanceMap);


    public final void updateInstanceHelper(Object object, I instance, InstanceMap instanceMap) {
        updateInstance(entityType.cast(object), instance, instanceMap);
    }

    public abstract void updateInstance(T model, I instance, InstanceMap instanceMap);

    public Class<? extends T> getEntityType() {
        return entityType;
    }

    public Class<I> getInstanceType() {
        return instanceType;
    }

    public java.lang.reflect.Type getGenericType() {
        return genericType;
    }
}
