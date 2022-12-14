package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.instance.PrimitiveInstance;
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
        return convertValue(instance.get(field), modelInstanceMap);
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ModelInstanceMap instanceMap) {
        Object fieldValue = ReflectUtils.get(model, javaField);
        if(fieldValue == null) {
            if(nullable) {
                return InstanceUtils.nullInstance();
            }
            throw new InternalException("Field " + fieldName() + " can not be null");
        }
        if(ValueUtil.isPrimitiveType(fieldValue.getClass())) {
            return InstanceUtils.resolvePrimitiveValue(field.getType(), fieldValue);
        }
        return instanceMap.getInstance(fieldValue);
    }

    @Override
    @JsonIgnore
    @SuppressWarnings("unused")
    public PojoDef<?> getDeclaringTypeDef() {
        return declaringTypeDef;
    }

    private Object convertValue(Instance value, ModelInstanceMap modelInstanceMap) {
        if(value instanceof PrimitiveInstance primitiveInstance) {
            if(primitiveInstance.isNull()) {
                return null;
            }
            if(primitiveInstance.isPassword()) {
                return ((Password) primitiveInstance.getValue()).getPassword();
            }
            return primitiveInstance.getValue();
        }
        else {
            return modelInstanceMap.getModel(Object.class, value);
        }
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
