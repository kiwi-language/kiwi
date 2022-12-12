package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.AnyType;

import java.util.Map;

public class AnyTypeDef<T> extends ModelDef<T, Instance> {

    private final AnyType type;

    public AnyTypeDef(Class<T> javaClass, AnyType type) {
        super(javaClass, Instance.class);
        this.type = type;
    }

    @Override
    public AnyType getType() {
        return type;
    }

    @Override
    public void initModel(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {

    }

    @Override
    public void updateModel(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {

    }

    @Override
    public void initInstance(Instance instance, Object model, ModelInstanceMap instanceMap) {

    }

    @Override
    public void updateInstance(Instance instance, Object model, ModelInstanceMap instanceMap) {

    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of(Object.class, type);
    }
}
