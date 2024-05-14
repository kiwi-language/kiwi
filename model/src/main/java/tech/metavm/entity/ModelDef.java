package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDef;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class ModelDef<T, I extends DurableInstance> implements Mapper<T, I> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ModelDef.class);

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

    public abstract TypeDef getTypeDef();

    public Type getType() {
        return getTypeDef().getType();
    }

    public List<Object> getEntities() {
        return List.of(getTypeDef());
    }

    public abstract void initEntity(T model, I instance, ObjectInstanceMap objectInstanceMap);

    public abstract void updateEntity(T model, I instance, ObjectInstanceMap objectInstanceMap);

    public abstract void initInstance(I instance, T model, ObjectInstanceMap instanceMap);

    public I createInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        I instance = InstanceFactory.allocate(instanceType, getTypeDef().getType(), id, EntityUtils.isEphemeral(model));
        initInstance(instance, model, instanceMap);
        return instance;
    }

    public abstract void updateInstance(I instance, T model, ObjectInstanceMap instanceMap);

    @Override
    public Class<T> getEntityClass() {
        return javaClass;
    }

    public Class<I> getInstanceClass() {
        return instanceType;
    }

    public java.lang.reflect.Type getEntityType() {
        return javaType;
    }

    public Map<Object, DurableInstance> getInstanceMapping() {
        return Map.of();
    }

    public <R> ModelDef<R, I> as(Class<R> javaClass) {
        if(javaClass.isAssignableFrom(this.javaClass))
            //noinspection unchecked
            return (ModelDef<R, I>) this;
        else
            throw new ClassCastException(javaClass.getName() + " is not assignable from " + this.javaClass.getName());
    }

    public T allocateEntity() {
        return ReflectionUtils.allocateInstance(javaClass);
    }

    public boolean isProxySupported() {
        return false;
    }

    public T createModelProxy(Class<? extends T> proxyClass) {
        throw new UnsupportedOperationException();
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
