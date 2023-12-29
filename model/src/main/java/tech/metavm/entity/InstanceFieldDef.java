package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;

@SuppressWarnings("ClassCanBeRecord")
public class InstanceFieldDef implements IFieldDef {

    private final Field javaField;
    private final tech.metavm.object.type.Field field;
    private final PojoDef<?> declaringTypeDef;

    public InstanceFieldDef(Field javaField, tech.metavm.object.type.Field field, PojoDef<?> declaringTypeDef) {
        this.javaField = javaField;
        this.field = field;
        this.declaringTypeDef = declaringTypeDef;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public void setModelField(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        ReflectionUtils.set(model, javaField, instance.getField(field));
    }

    @Override
    public Instance getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        return instance.getField(field);
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap) {
        return (Instance) NncUtils.requireNonNull(ReflectionUtils.get(model, javaField));
    }

    @Override
    public PojoDef<?> getDeclaringTypeDef() {
        return declaringTypeDef;
    }

    @Override
    public Field getJavaField() {
        return javaField;
    }

    @Override
    public tech.metavm.object.type.Field getField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }
}
