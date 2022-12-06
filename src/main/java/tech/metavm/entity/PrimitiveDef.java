package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;

import java.util.Map;

public class PrimitiveDef<T> extends ModelDef<T, Instance> {

    private final Type type;

    public PrimitiveDef(Class<T> modelType, Type type) {
        super(modelType, Instance.class);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void initModel(T model, Instance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, Instance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(Instance instance, T model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(T model, Instance instance, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of(getModelType(), type);
    }
}
