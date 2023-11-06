package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ReflectUtils;

import java.util.Map;

public class DirectDef<T> extends ModelDef<T, ClassInstance> {

    private final Type type;
    private final Class<?> nativeClass;

    public DirectDef(java.lang.reflect.Type javaType, Type type) {
        this(javaType, type, null);
    }

    public DirectDef(java.lang.reflect.Type javaType, Type type, Class<?> nativeClass) {
        //noinspection rawtypes,unchecked
        super((Class) ReflectUtils.getRawClass(javaType), javaType, ClassInstance.class);
        this.type = type;
        this.nativeClass = nativeClass;
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

    public Class<?> getNativeClass() {
        return nativeClass;
    }
}
