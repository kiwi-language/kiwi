package tech.metavm.entity;

import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.type.Type;

import java.util.Map;

public class PrimitiveDef<T> extends ModelDef<T, PrimitiveInstance> {

    private final Type type;

    public PrimitiveDef(Class<T> modelType, Type type) {
        super(modelType, PrimitiveInstance.class);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void initModel(T model, PrimitiveInstance instance, ModelInstanceMap modelInstanceMap) {
    }

    @Override
    public void updateModel(T model, PrimitiveInstance instance, ModelInstanceMap modelInstanceMap) {
    }

    @Override
    public void initInstance(PrimitiveInstance instance, T model, ModelInstanceMap instanceMap) {
    }

    @Override
    public void updateInstance(PrimitiveInstance instance, T model, ModelInstanceMap instanceMap) {
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of(getJavaClass(), type);
    }
}
