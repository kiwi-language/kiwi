package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.type.Type;
import tech.metavm.util.ReflectionUtils;

public class DirectDef<T> extends ModelDef<T, ClassInstance> {

    private final Type type;
    private final Class<?> nativeClass;

    public DirectDef(java.lang.reflect.Type javaType, Type type) {
        this(javaType, type, null);
    }

    public DirectDef(java.lang.reflect.Type javaType, Type type, Class<?> nativeClass) {
        //noinspection rawtypes,unchecked
        super((Class) ReflectionUtils.getRawClass(javaType), javaType, ClassInstance.class);
        this.type = type;
        this.nativeClass = nativeClass;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void initModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    public Class<?> getNativeClass() {
        return nativeClass;
    }
}
