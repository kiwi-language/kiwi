package tech.metavm.entity;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ReflectUtils;

import java.util.Map;

public class DirectDef<T> extends ModelDef<T, ClassInstance> {

    private final Type type;

    public DirectDef(java.lang.reflect.Type javaType, Type type) {
        //noinspection rawtypes,unchecked
        super((Class) ReflectUtils.getRawClass(javaType), javaType, ClassInstance.class);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void initModel(T model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(ClassInstance instance, T model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of();
    }
}
