package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.TypeVariable;

import java.util.Map;

public class TypeVariableDef extends ModelDef<Object, ClassInstance> {

    private final TypeVariable type;

    public TypeVariableDef(java.lang.reflect.TypeVariable<?> javaTypeVariable, TypeVariable type) {
        super(Object.class, javaTypeVariable, ClassInstance.class);
        this.type = type;
    }

    @Override
    public TypeVariable getType() {
        return type;
    }

    @Override
    public void initModel(Object model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(Object model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(ClassInstance instance, Object model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, Object model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of();
    }
}
