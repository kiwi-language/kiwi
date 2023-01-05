package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.AnyType;
import tech.metavm.object.meta.Type;

import java.util.Map;

public class InstanceDef<I extends Instance> extends ModelDef<I, I> {

    private final Type type;

    protected InstanceDef(Class<I> instanceClass) {
        super(instanceClass, instanceClass);
        this.type = new AnyType();
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
