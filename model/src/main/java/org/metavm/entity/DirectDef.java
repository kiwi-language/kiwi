package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.TypeDef;
import org.metavm.util.ReflectionUtils;

public class DirectDef<T> extends ModelDef<T, ClassInstance> {

    private final TypeDef typeDef;
    private final Class<?> nativeClass;

    public DirectDef(java.lang.reflect.Type javaType, TypeDef typeDef) {
        this(javaType, typeDef, null);
    }

    public DirectDef(java.lang.reflect.Type javaType, TypeDef typeDef, Class<?> nativeClass) {
        //noinspection rawtypes,unchecked
        super((Class) ReflectionUtils.getRawClass(javaType), javaType, ClassInstance.class);
        this.typeDef = typeDef;
        this.nativeClass = nativeClass;
    }

    @Override
    public TypeDef getTypeDef() {
        return typeDef;
    }

    @Override
    public void initEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
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
