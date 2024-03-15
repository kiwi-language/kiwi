package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.type.AnyType;
import tech.metavm.object.type.Type;

public class InstanceDef<I extends DurableInstance> extends ModelDef<I, I> {

    private final AnyType type;

    protected InstanceDef(Class<I> instanceClass, AnyType type) {
        super(instanceClass, instanceClass);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public I createInstance(I model, ObjectInstanceMap instanceMap, Id id) {
        return model;
    }

    @Override
    public I createModel(I instance, ObjectInstanceMap modelIMap) {
        return instance;
    }

    @Override
    public void initModel(I model, I instance, ObjectInstanceMap modelIMap) {

    }

    @Override
    public void updateModel(I model, I instance, ObjectInstanceMap modelIMap) {

    }

    @Override
    public void initInstance(I instance, I model, ObjectInstanceMap instanceMap) {

    }

    @Override
    public void updateInstance(I instance, I model, ObjectInstanceMap instanceMap) {

    }

    @Override
    public boolean isProxySupported() {
        return false;
    }

}
