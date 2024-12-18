package org.metavm.entity;

import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Mapper<T, I extends Instance> {

    Logger logger = LoggerFactory.getLogger(Mapper.class);

    default T createEntityHelper(Instance instance, ObjectInstanceMap objectInstanceMap) {
        return createEntity(getInstanceClass().cast(instance), objectInstanceMap);
    }

    default T createEntity(I instance, ObjectInstanceMap objectInstanceMap) {
        T model = allocateEntity();
        if(model instanceof IdInitializing idInitializing) {
            var d = (Instance) instance;
            if(d.tryGetId() != null)
                idInitializing.initId(d.tryGetId());
        }
        initEntity(model, instance, objectInstanceMap);
        return model;
    }

    default void updateInstanceHelper(Object entity, Instance instance, ObjectInstanceMap map) {
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

    default void updateEntityHelper(Object model, Instance instance, ObjectInstanceMap objectInstanceMap) {
        updateEntity(getEntityClass().cast(model), getInstanceClass().cast(instance), objectInstanceMap);
    }

    void initEntity(T model, I instance, ObjectInstanceMap objectInstanceMap);

    void updateEntity(T model, I instance, ObjectInstanceMap objectInstanceMap);

    default I allocateInstanceHelper(Object model, ObjectInstanceMap instanceMap, Id id) {
       return allocateInstance(getEntityClass().cast(model), instanceMap, id);
    }

    default I allocateInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        return InstanceFactory.allocate(getInstanceClass(), id, EntityUtils.isEphemeral(model));
    }

    default I createInstanceHelper(Object model, ObjectInstanceMap instanceMap, Id id) {
        return createInstance(getEntityClass().cast(model), instanceMap, id);
    }

    default I createInstance(T model, ObjectInstanceMap instanceMap, Id id) {
        I instance = InstanceFactory.allocate(getInstanceClass(), id, EntityUtils.isEphemeral(model));
        instanceMap.addMapping(model, instance);
        initInstance(instance, model, instanceMap);
        return instance;
    }

    default void initInstanceHelper(Instance instance, Object model, ObjectInstanceMap instanceMap) {
        initInstance(getInstanceClass().cast(instance), getEntityClass().cast(model), instanceMap);
    }

    Class<T> getEntityClass();

    Class<I> getInstanceClass();

    boolean isProxySupported();

    default <R> Mapper<R, I> as(Class<R> javaClass) {
        if(javaClass.isAssignableFrom(getEntityClass()))
            //noinspection unchecked
            return (Mapper<R, I>) this;
        throw new ClassCastException(javaClass.getName() + " is not assignable from " + getEntityClass().getName());
    }

    default boolean isDisabled() {
        return false;
    }

}
