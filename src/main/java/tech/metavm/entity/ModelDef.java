package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TypeReference;

import java.util.Map;

public abstract class ModelDef<T, I extends Instance> {

    private final Class<T> modelType;
    private final java.lang.reflect.Type genericType;
    private final Class<I> instanceType;

    protected ModelDef(Class<T> modelType, Class<I> instanceType) {
        this(modelType, modelType, instanceType);
    }

    protected ModelDef(TypeReference<T> typeReference, Class<I> instanceType) {
        this(typeReference.getType(), typeReference.getGenericType(), instanceType);
    }

    private ModelDef(Class<T> modelType, java.lang.reflect.Type genericType, Class<I> instanceType) {
        this.modelType = modelType;
        this.genericType = genericType;
        this.instanceType = instanceType;
    }

    public abstract Type getType();

    public abstract void initModel(T model, I instance, ModelInstanceMap modelInstanceMap);

    public void initModelHelper(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {
        initModel(modelType.cast(model), instanceType.cast(instance), modelInstanceMap);
    }

    public abstract void updateModel(T model, I instance, ModelInstanceMap modelInstanceMap);

    public abstract void initInstance(I instance, T model, ModelInstanceMap instanceMap);

    public final I createInstanceHelper(Object model, ModelInstanceMap instanceMap) {
        return createInstance(modelType.cast(model), instanceMap);
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
        initInstance(instanceType.cast(instance), modelType.cast(model), instanceMap);
    }

    public final void updateInstanceHelper(Object object, Instance instance, ModelInstanceMap instanceMap) {
        updateInstance(modelType.cast(object), instanceType.cast(instance), instanceMap);
    }

    public abstract void updateInstance(T model, I instance, ModelInstanceMap instanceMap);

    public Class<? extends T> getModelType() {
        return modelType;
    }

    public Class<I> getInstanceType() {
        return instanceType;
    }

    public java.lang.reflect.Type getGenericType() {
        return genericType;
    }

    public abstract Map<Object, Identifiable> getEntityMapping();

    public Map<Object, Instance> getInstanceMapping() {
        return Map.of();
    }

    public T createModelHelper(Instance instance, ModelInstanceMap modelInstanceMap) {
        return createModel(instanceType.cast(instance), modelInstanceMap);
    }
    public T createModel(I instance, ModelInstanceMap modelInstanceMap) {
        T model = ReflectUtils.allocateInstance(modelType);
        if(model instanceof IdInitializing idInitializing && instance.getId() != null) {
            idInitializing.initId(instance.getId());
        }
        initModel(model, instance, modelInstanceMap);
        return model;
    }

    public boolean isProxySupported() {
        return true;
    }

    public T createModelProxy(Class<? extends T> proxyClass) {
        return null;
    }

}
