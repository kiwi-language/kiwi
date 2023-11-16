package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TypeReference;

import java.util.Map;

public abstract class ModelDef<T, I extends Instance> {

    private final Class<T> javaClass;
    private final java.lang.reflect.Type javaType;
    private final Class<I> instanceType;

    protected ModelDef(Class<T> javaClass, Class<I> instanceType) {
        this(javaClass, javaClass, instanceType);
    }

    protected ModelDef(TypeReference<T> typeReference, Class<I> instanceType) {
        this(typeReference.getType(), typeReference.getGenericType(), instanceType);
    }

    ModelDef(Class<T> javaClass, java.lang.reflect.Type javaType, Class<I> instanceType) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.instanceType = instanceType;
    }

    public abstract Type getType();

    public abstract void initModel(T model, I instance, ModelInstanceMap modelInstanceMap);

    public void initModelHelper(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {
        initModel(javaClass.cast(model), instanceType.cast(instance), modelInstanceMap);
    }

    protected void reloadParent(Entity entity, Instance instance, ModelInstanceMap instanceMap, DefContext defContext) {
        if(entity.getParentEntity() != null) {
            var parent = instanceMap.getInstance(entity.getParentEntity());
            Field parentField = null;
            if(entity.getParentEntityField() != null) {
                parentField = defContext.getField(entity.getParentEntityField());
            }
            instance.reloadParent(new InstanceParentRef(parent, parentField));
        }
        else {
            instance.reloadParent(null);
        }
    }

    public abstract void updateModel(T model, I instance, ModelInstanceMap modelInstanceMap);

    public abstract void initInstance(I instance, T model, ModelInstanceMap instanceMap);

    public final I createInstanceHelper(Object model, ModelInstanceMap instanceMap) {
        return createInstance(javaClass.cast(model), instanceMap);
    }

    public I createInstance(T model, ModelInstanceMap instanceMap) {
        I instance = InstanceFactory.allocate(instanceType, getType());
        if((model instanceof Identifiable identifiable) && identifiable.getId() != null) {
            instance.initId(identifiable.getId());
        }
        initInstance(instance, model, instanceMap);
        return instance;
    }

    public final void initInstanceHelper(Instance instance, Object model, ModelInstanceMap instanceMap) {
        initInstance(instanceType.cast(instance), javaClass.cast(model), instanceMap);
    }

    public final void updateInstanceHelper(Object object, Instance instance, ModelInstanceMap instanceMap) {
        updateInstance(instanceType.cast(instance), javaClass.cast(object), instanceMap);
    }

    public abstract void updateInstance(I instance, T model, ModelInstanceMap instanceMap);

    public Class<? extends T> getJavaClass() {
        return javaClass;
    }

    public Class<I> getInstanceType() {
        return instanceType;
    }

    public java.lang.reflect.Type getJavaType() {
        return javaType;
    }

    public abstract Map<Object, Identifiable> getEntityMapping();

    public Map<Object, Instance> getInstanceMapping() {
        return Map.of();
    }

    public T createModelHelper(Instance instance, ModelInstanceMap modelInstanceMap) {
        return createModel(instanceType.cast(instance), modelInstanceMap);
    }
    public T createModel(I instance, ModelInstanceMap modelInstanceMap) {
        T model = allocateModel();
        if(model instanceof IdInitializing idInitializing && instance.getId() != null) {
            idInitializing.initId(instance.getId());
        }
        initModel(model, instance, modelInstanceMap);
        return model;
    }

    protected T allocateModel() {
        return ReflectUtils.allocateInstance(javaClass);
    }

    public boolean isProxySupported() {
        return false;
    }

    public T createModelProxy(Class<? extends T> proxyClass) {
        throw new UnsupportedOperationException();
    }

    public T createModelProxyHelper(Class<?> proxyClass) {
        return createModelProxy(proxyClass.asSubclass(javaClass));
    }

}
