package org.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class FieldDef implements IFieldDef {

    private static final Logger logger = LoggerFactory.getLogger(FieldDef.class);

    private final Field javaField;
    private final PojoDef<?> declaringTypeDef;
    private final org.metavm.object.type.Field field;
    private final @Nullable Mapper<?, ?> targetMapper;

    public FieldDef(org.metavm.object.type.Field field,
                    boolean nullable,
                    Field javaField,
                    PojoDef<?> declaringTypeDef,
                    @Nullable Mapper<?, ?> targetMapper) {
        this.javaField = javaField;
        this.targetMapper = targetMapper;
        this.declaringTypeDef = declaringTypeDef;
        this.field = field;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public void setModelField(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Object fieldValue = getModelFieldValue(instance, objectInstanceMap);
        ReflectionUtils.set(model, javaField, fieldValue);
    }

    @Override
    public Object getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Value fieldValue = instance.getField(field);
        if (targetMapper instanceof InstanceArrayMapper<?, ?>) {
//            noinspection rawtypes,unchecked
            return objectInstanceMap.getEntity(javaField.getType(), fieldValue, (Mapper) targetMapper);
        }
        return objectInstanceMap.getEntity(javaField.getType(), fieldValue);
    }

    @Override
    public Value getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap) {
        EntityUtils.ensureProxyInitialized(model);
        return instanceMap.getInstance(ReflectionUtils.get(model, javaField));
    }

    @Override
    @JsonIgnore
    @SuppressWarnings("unused")
    public PojoDef<?> getDeclaringTypeDef() {
        return declaringTypeDef;
    }

    private String fieldName() {
        return javaField.getDeclaringClass().getName() + "." + javaField.getName();
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
