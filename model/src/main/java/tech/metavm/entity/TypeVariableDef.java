package tech.metavm.entity;

import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.TypeDef;
import tech.metavm.object.type.TypeVariable;

public class TypeVariableDef extends ModelDef<Object, ClassInstance> {

    private final TypeVariable variable;

    public TypeVariableDef(java.lang.reflect.TypeVariable<?> javaTypeVariable, TypeVariable variable) {
        super(Object.class, javaTypeVariable, ClassInstance.class);
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
