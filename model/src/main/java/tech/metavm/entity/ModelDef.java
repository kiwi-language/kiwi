package tech.metavm.entity;

import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class ModelDef<T, I extends DurableInstance> {

    protected final Class<T> javaClass;
    protected final java.lang.reflect.Type javaType;
    private final Class<I> instanceType;
    private boolean initialized;
    @Nullable
    private DefParser<T,I,?> parser;

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

    public List<Object> getEntities() {
        return List.of(getType());
    }

    public abstract void initModel(T model, I instance, ObjectInstanceMap objectInstanceMap);

    public void initModelHelper(Object model, Instance instance, ObjectInstanceMap objectInstanceMap) {
        initModel(javaClass.cast(model), instanceType.cast(instance), objectInstanceMap);
    }

    protected void reloadParent(Entity entity, DurableInstance instance, ObjectInstanceMap instanceMap, DefContext defContext) {
        if(entity.getParentEntity() != null) {
            var parent = (DurableInstance) instanceMap.getInstance(entity.getParentEntity());
            Field parentField = null;
            if(entity.getParentEntityField() != null)
                parentField = defContext.getField(entity.getParentEntityField());
            instance.setParentInternal(parent, parentField);
        }
        else {
            instance.setParentInternal(null, null);
        }
    }

    public abstract void updateModel(T model, I instance, ObjectInstanceMap objectInstanceMap);

    public abstract void initInstance(I instance, T model, ObjectInstanceMap instanceMap);

    public final I createInstanceHelper(Object model, ObjectInstanceMap instanceMap, Long id) {
        return createInstance(javaClass.cast(model), instanceMap, id);
    }

    public I createInstance(T model, ObjectInstanceMap instanceMap, Long id) {
        I instance = InstanceFactory.allocate(instanceType, getType(), NncUtils.get(id, PhysicalId::new), EntityUtils.isEphemeral(model));
        initInstance(instance, model, instanceMap);
        return instance;
    }

    public final void initInstanceHelper(Instance instance, Object model, ObjectInstanceMap instanceMap) {
        initInstance(instanceType.cast(instance), javaClass.cast(model), instanceMap);
    }

    public final void updateInstanceHelper(Object object, Instance instance, ObjectInstanceMap instanceMap) {
        updateInstance(instanceType.cast(instance), javaClass.cast(object), instanceMap);
    }

    public abstract void updateInstance(I instance, T model, ObjectInstanceMap instanceMap);

    public Class<T> getJavaClass() {
        return javaClass;
    }

    public Class<I> getInstanceType() {
        return instanceType;
    }

    public java.lang.reflect.Type getJavaType() {
        return javaType;
    }

    public Map<Object, DurableInstance> getInstanceMapping() {
        return Map.of();
    }

    public T createModelHelper(Instance instance, ObjectInstanceMap objectInstanceMap) {
        return createModel(instanceType.cast(instance), objectInstanceMap);
    }
    public T createModel(I instance, ObjectInstanceMap objectInstanceMap) {
        T model = allocateModel();
        if(model instanceof IdInitializing idInitializing) {
            var d = (DurableInstance) instance;
            if(d.tryGetPhysicalId() != null)
                idInitializing.initId(d.tryGetPhysicalId());
        }
        initModel(model, instance, objectInstanceMap);
        return model;
    }

    public <R> ModelDef<R, ?> as(Class<R> javaClass) {
        if(javaClass.isAssignableFrom(this.javaClass))
            //noinspection unchecked
            return (ModelDef<R, ?>) this;
        else
            throw new ClassCastException();
    }

    protected T allocateModel() {
        return ReflectionUtils.allocateInstance(javaClass);
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

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Nullable
    public DefParser<T, I, ?> getParser() {
        return parser;
    }

    public void setParser(@Nullable DefParser<T, I, ?> parser) {
        this.parser = parser;
    }
}
