package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.AnyType;
import org.metavm.object.type.Type;
import org.metavm.util.ReflectionUtils;

public class InstanceMapper<I extends DurableInstance> implements Mapper<I, I> {

    private final AnyType type;
    private final Class<I> instanceClass;

    protected InstanceMapper(Class<I> instanceClass) {
        this.instanceClass = instanceClass;
        this.type = AnyType.instance;
    }

    @Override
    public Class<I> getInstanceClass() {
        return instanceClass;
    }

    @Override
    public Class<I> getEntityClass() {
        return instanceClass;
    }

    public java.lang.reflect.Type getEntityType() {
        return instanceClass;
    }

    public Type getType() {
        return type;
    }

    @Override
    public I createInstance(I model, ObjectInstanceMap map, Id id) {
        return model;
    }

    @Override
    public I createEntity(I instance, ObjectInstanceMap map) {
        return instance;
    }

    @Override
    public void initEntity(I model, I instance, ObjectInstanceMap map) {

    }

    @Override
    public void updateEntity(I model, I instance, ObjectInstanceMap map) {

    }

    @Override
    public void initInstance(I instance, I model, ObjectInstanceMap map) {

    }

    @Override
    public I allocateEntity() {
        return ReflectionUtils.allocateInstance(instanceClass);
    }

    @Override
    public I createModelProxy(Class<? extends I> proxyClass) {
        return null;
    }

    @Override
    public void updateInstance(I instance, I model, ObjectInstanceMap map) {

    }

    @Override
    public boolean isProxySupported() {
        return false;
    }

}
