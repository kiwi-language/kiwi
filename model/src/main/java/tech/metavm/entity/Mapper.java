package tech.metavm.entity;

import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

public interface Mapper<T, I extends DurableInstance> {

    default T createEntityHelper(Instance instance, ObjectInstanceMap objectInstanceMap) {
        return createEntity(getInstanceClass().cast(instance), objectInstanceMap);
    }

    default T createEntity(I instance, ObjectInstanceMap objectInstanceMap) {
        T model = allocateEntity();
        if(model instanceof IdInitializing idInitializing) {
            var d = (DurableInstance) instance;
            if(d.tryGetPhysicalId() != null)
                idInitializing.initId(d.tryGetId());
        }
        initEntity(model, instance, objectInstanceMap);
        return model;
    }

    default void updateInstanceHelper(Object entity, Instance instance,ObjectInstanceMap map) {
        updateInstance(getInstanceClass().cast(instance), getEntityClass().cast(entity), map);
    }

    void updateInstance(I instance, T entity, ObjectInstanceMap map);

    void initInstance(I instance, T entity, ObjectInstanceMap map);

    T allocateEntity();

    default T createModelProxyHelper(Class<?> proxyClass) {
        return createModelProxy(proxyClass.asSubclass(getEntityClass()));
    }

    T createModelProxy(Class<? extends T> proxyClass);

    default void initEntityHelper(Object model, Instance instance, ObjectInstanceMap objectInstanceMap) {
        initEntity(getEntityClass().cast(model), getInstanceClass().cast(instance), objectInstanceMap);
    }

    void initEntity(T model, I instance, ObjectInstanceMap objectInstanceMap);

    void updateEntity(T model, I instance, ObjectInstanceMap objectInstanceMap);

    default I createInstanceHelper(Object model, ObjectInstanceMap instanceMap, Id id) {
        return createInstance(getEntityClass().cast(model), instanceMap, id);
    }

    default I createInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        I instance = InstanceFactory.allocate(getInstanceClass(), getType(), id, EntityUtils.isEphemeral(model));
        initInstance(instance, model, instanceMap);
        return instance;
    }

    default void initInstanceHelper(Instance instance, Object model, ObjectInstanceMap instanceMap) {
        initInstance(getInstanceClass().cast(instance), getEntityClass().cast(model), instanceMap);
    }

    Class<T> getEntityClass();

    java.lang.reflect.Type getEntityType();

    Class<I> getInstanceClass();

    Type getType();

    boolean isProxySupported();

    default <R> Mapper<R, I> as(Class<R> javaClass) {
        if(javaClass.isAssignableFrom(getEntityClass()))
            //noinspection unchecked
            return (Mapper<R, I>) this;
        throw new ClassCastException(javaClass.getName() + " is not assignable from " + getEntityClass().getName());
    }

}
