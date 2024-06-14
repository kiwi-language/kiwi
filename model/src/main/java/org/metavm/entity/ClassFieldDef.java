package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;

public class ClassFieldDef implements IFieldDef {

    private final PojoDef<?> declaringTypeDef;
    private final org.metavm.object.type.Field field;
    private final Field javaField;
    private final DefMap defMap;

    public ClassFieldDef(PojoDef<?> declaringTypeDef,
                         org.metavm.object.type.Field field,
                         Field javaField,
                         DefMap defMap) {
        this.declaringTypeDef = declaringTypeDef;
        this.field = field;
        this.javaField = javaField;
        this.defMap = defMap;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public Object getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Instance instanceFieldValue = instance.getField(field);
        if(instanceFieldValue.isNull())
            return null;
        Klass type = objectInstanceMap.getEntity(Klass.class, instanceFieldValue);
        return defMap.getMapper(type.getType()).getEntityClass();
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap) {
        Class<?> fieldValue = (Class<?>) ReflectionUtils.get(model, javaField);
        return fieldValue != null ?
                instanceMap.getInstance(defMap.getType(fieldValue))
                : Instances.nullInstance();
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
