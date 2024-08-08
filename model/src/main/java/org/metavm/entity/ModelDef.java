package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDef;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class ModelDef<T> implements Mapper<T, ClassInstance> {

    public static final Logger logger = LoggerFactory.getLogger(ModelDef.class);

    protected final Class<T> javaClass;
    protected final java.lang.reflect.Type javaType;
    //    private final Class<I> instanceType;
    private boolean initialized;
    @Nullable
    private DefParser<T, ?> parser;
    private volatile boolean disabled;

    protected ModelDef(Class<T> javaClass) {
        this(javaClass, javaClass);
    }

    protected ModelDef(TypeReference<T> typeReference) {
        this(typeReference.getType(), typeReference.getGenericType());
    }

    ModelDef(Class<T> javaClass, java.lang.reflect.Type javaType) {
        this.javaClass = javaClass;
        this.javaType = javaType;
    }

    public abstract TypeDef getTypeDef();

    public Type getType() {
        return getTypeDef().getType();
    }

    public List<Object> getEntities() {
        return List.of(getTypeDef());
    }

    public abstract void initEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap);

    public abstract void updateEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap);

    public abstract void initInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap);

    @Override
    public ClassInstance allocateInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        return ClassInstanceBuilder.newBuilder((ClassType) instanceMap.getType(EntityUtils.getRealType(model)))
                .ephemeral(EntityUtils.isEphemeral(model))
                .id(id)
                .build();
    }

    public ClassInstance createInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        var instance = allocateInstance(model, instanceMap, id);
        instanceMap.addMapping(model, instance);
        initInstance(instance, model, instanceMap);
        return instance;
    }

    public abstract void updateInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap);

    @Override
    public Class<T> getEntityClass() {
        return javaClass;
    }

    @Override
    public Class<ClassInstance> getInstanceClass() {
        return ClassInstance.class;
    }

    public java.lang.reflect.Type getEntityType() {
        return javaType;
    }

    public Map<Object, Instance> getInstanceMapping() {
        return Map.of();
    }

    public <R> ModelDef<R> as(Class<R> javaClass) {
        if (javaClass.isAssignableFrom(this.javaClass))
            //noinspection unchecked
            return (ModelDef<R>) this;
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
    public DefParser<T, ?> getParser() {
        return parser;
    }

    public void setParser(@Nullable DefParser<T, ?> parser) {
        this.parser = parser;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
