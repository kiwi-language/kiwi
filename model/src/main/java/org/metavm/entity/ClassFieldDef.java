package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ClassFieldDef implements IFieldDef {

    private static final Logger logger = LoggerFactory.getLogger(ClassFieldDef.class);

    private final PojoDef<?> declaringTypeDef;
    private final org.metavm.object.type.Field field;
    private final Field javaField;
    private final DefContext defContext;

    public ClassFieldDef(PojoDef<?> declaringTypeDef,
                         org.metavm.object.type.Field field,
                         Field javaField,
                         DefContext defContext) {
        this.declaringTypeDef = declaringTypeDef;
        this.field = field;
        this.javaField = javaField;
        this.defContext = defContext;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public Object getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Value instanceFieldValue = instance.getField(field);
        if(instanceFieldValue.isNull())
            return null;
        Klass type = objectInstanceMap.getEntity(Klass.class, instanceFieldValue);
        return defContext.getMapper(type.getType()).getEntityClass();
    }

    @Override
    public Value getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap) {
        Class<?> fieldValue = (Class<?>) ReflectionUtils.get(model, javaField);
        return fieldValue != null ?
                instanceMap.getInstance(defContext.getKlass(fieldValue))
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
