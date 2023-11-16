package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.type.ObjectType;
import tech.metavm.object.type.Type;

import java.util.Map;

public class InstanceDef<I extends Instance> extends ModelDef<I, I> {

    private final ObjectType type;

    protected InstanceDef(Class<I> instanceClass, ObjectType type) {
        super(instanceClass, instanceClass);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public I createInstance(I model, ModelInstanceMap instanceMap) {
        return model;
    }

    @Override
    public I createModel(I instance, ModelInstanceMap modelIMap) {
        return instance;
    }

    @Override
    public void initModel(I model, I instance, ModelInstanceMap modelIMap) {

    }

    @Override
    public void updateModel(I model, I instance, ModelInstanceMap modelIMap) {

    }

    @Override
    public void initInstance(I instance, I model, ModelInstanceMap instanceMap) {

    }

    @Override
    public void updateInstance(I instance, I model, ModelInstanceMap instanceMap) {

    }

    @Override
    public boolean isProxySupported() {
        return false;
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of();
    }
}
