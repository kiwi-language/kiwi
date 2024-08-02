package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.NullValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;

@SuppressWarnings("ClassCanBeRecord")
public class InstanceFieldDef implements IFieldDef {

    private final Field javaField;
    private final org.metavm.object.type.Field field;
    private final PojoDef<?> declaringTypeDef;

    public InstanceFieldDef(Field javaField, org.metavm.object.type.Field field, PojoDef<?> declaringTypeDef) {
        this.javaField = javaField;
        this.field = field;
        this.declaringTypeDef = declaringTypeDef;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public void setModelField(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        ReflectionUtils.set(model, javaField, getModelFieldValue(instance, objectInstanceMap));
    }

    @Override
    public Value getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        var value = instance.getField(field);
        if(value.isNull())
            return javaField.getType().isAssignableFrom(NullValue.class) ? value : null;
        else
            return value;
    }

    @Override
    public Value getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap) {
        return (Value) NncUtils.orElse(ReflectionUtils.get(model, javaField), Instances.nullInstance());
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
    public org.metavm.object.type.Field getField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }
}
