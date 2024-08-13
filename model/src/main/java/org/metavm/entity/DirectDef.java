package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.TypeDef;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Type;

public class DirectDef<T> extends ModelDef<T> {

    private final TypeDef typeDef;

    public DirectDef(Type javaType, TypeDef typeDef) {
        //noinspection rawtypes,unchecked
        super((Class) ReflectionUtils.getRawClass(javaType), javaType);
        this.typeDef = typeDef;
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

    @Override
    public boolean isDisabled() {
        return true;
    }
}
