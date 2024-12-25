package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@Slf4j
public class ElementValueDef<T extends Value> extends PojoDef<T> {

    public ElementValueDef(Class<T> javaType, Type genericType, @Nullable PojoDef<? super T> parentDef, Klass type, DefContext defContext) {
        super(javaType, genericType, parentDef, type, defContext);
    }

    @Override
    public void initEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createEntity(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEntity(T pojo, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
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

}
