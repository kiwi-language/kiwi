package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.object.type.TypeDef;
import org.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

public class DirectDef<T> extends ModelDef<T> {

    private final TypeDef typeDef;
    private final @Nullable StaticFieldTable staticFieldTable;

    public DirectDef(Type javaType, TypeDef typeDef) {
        this(javaType, typeDef, null);
    }

    public DirectDef(Type javaType, TypeDef typeDef, @Nullable StaticFieldTable staticFieldTable) {
        //noinspection rawtypes,unchecked
        super((Class) ReflectionUtils.getRawClass(javaType), javaType);
        this.typeDef = typeDef;
        this.staticFieldTable = staticFieldTable;
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

    @Override
    public List<Object> getEntities() {
        if(staticFieldTable == null)
            return List.of(typeDef);
        else
            return List.of(typeDef, staticFieldTable);
    }
}
