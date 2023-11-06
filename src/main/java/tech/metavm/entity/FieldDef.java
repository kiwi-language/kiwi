package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.util.*;

import java.lang.reflect.Field;

public class FieldDef implements IFieldDef {

    private final Field javaField;
    private final boolean nullable;
    private final PojoDef<?> declaringTypeDef;
    private final tech.metavm.object.meta.Field field;
    private final ModelDef<?, ?> targetDef;

    public FieldDef(tech.metavm.object.meta.Field field,
                    boolean nullable,
                    Field javaField,
                    PojoDef<?> declaringTypeDef,
                    ModelDef<?, ?> targetDef) {
        this.javaField = javaField;
        this.nullable = nullable;
        this.targetDef = targetDef;
        this.declaringTypeDef = declaringTypeDef;
        this.field = field;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public void setModelField(Object model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        Object fieldValue = getModelFieldValue(instance, modelInstanceMap);
        ReflectUtils.set(model, javaField, fieldValue);
    }

    @Override
    public Object getModelFieldValue(ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        Instance fieldValue = instance.getField(field);
        if(fieldValue instanceof PrimitiveInstance primitiveInstance) {
            if(primitiveInstance.isNull()) {
                return null;
            }
            if(primitiveInstance instanceof  LongInstance longInstance &&
                    (javaField.getType() == int.class || javaField.getType() == Integer.class)) {
                return longInstance.getValue().intValue();
            }
            if(primitiveInstance instanceof PasswordInstance passwordInstance) {
                return new Password(passwordInstance);
            }
            return primitiveInstance.getValue();
        }
        if(targetDef instanceof InstanceDef<?> || targetDef instanceof InstanceCollectionDef<?,?>) {
            return modelInstanceMap.getModel(Object.class, fieldValue, targetDef);
        }
        return modelInstanceMap.getModel(Object.class, fieldValue);
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ModelInstanceMap instanceMap) {
        EntityUtils.ensureProxyInitialized(model);
        Object fieldValue = ReflectUtils.get(model, javaField);
        if(fieldValue == null) {
            if(nullable) {
                return InstanceUtils.nullInstance();
            }
            throw new InternalException("Field " + fieldName() + " can not be null");
        }
        if(ValueUtil.isPrimitiveType(fieldValue.getClass())) {
            return InstanceUtils.resolvePrimitiveValue(
                    field.getType(),
                    fieldValue,
                    declaringTypeDef.getDefMap()::getType
            );
        }
        return instanceMap.getInstance(fieldValue);
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
    public java.lang.reflect.Field getJavaField() {
        return javaField;
    }

    @Override
    public tech.metavm.object.meta.Field getField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public ModelDef<?, ?> getTargetDef() {
        return targetDef;
    }
}
