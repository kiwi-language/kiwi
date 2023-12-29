package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.type.TypeVariable;

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
    public void initModel(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
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
