package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.TypeVariable;

public class TypeVariableDef extends ModelDef<Object> {

    private final TypeVariable variable;

    public TypeVariableDef(java.lang.reflect.TypeVariable<?> javaTypeVariable, TypeVariable variable) {
        super(Object.class, javaTypeVariable);
        this.variable = variable;
    }

    @Override
    public TypeDef getTypeDef() {
        return variable;
    }

    @Override
    public void initEntity(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEntity(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(ClassInstance instance, Object model, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, Object model, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

}
