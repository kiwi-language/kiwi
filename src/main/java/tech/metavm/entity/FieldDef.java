package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.ValueUtil;

import java.lang.reflect.Field;

public class FieldDef {

    private final Field reflectField;
    private final PojoDef<?> declaringTypeDef;
    private final tech.metavm.object.meta.Field field;
    private final ModelDef<?, ?> targetDef;
    private final TypeCategory typeCategory;

    public FieldDef(tech.metavm.object.meta.Field field,
                    Field reflectField,
                    PojoDef<?> declaringTypeDef,
                    ModelDef<?, ?> targetDef) {
        this.reflectField = reflectField;
        typeCategory = ValueUtil.getTypeCategory(reflectField.getGenericType());
        this.targetDef = targetDef;
        if(typeCategory.isReference() && targetDef == null) {
            throw new InternalException("Can not find definition for type: " + reflectField.getGenericType());
        }
        this.declaringTypeDef = declaringTypeDef;
        this.field = field;
        declaringTypeDef.addFieldDef(this);
    }

    public void setModelField(Object entity, Instance instance, ModelInstanceMap modelInstanceMap) {
        Object fieldValue = getModelFieldValue(instance, modelInstanceMap);
        ReflectUtils.set(entity, reflectField, fieldValue);
    }

    public Object getModelFieldValue(Instance instance, ModelInstanceMap modelInstanceMap) {
        return convertValue(instance.get(field), declaringTypeDef.getModelType(), modelInstanceMap);
    }

    public Object getInstanceFieldValue(Object model, ModelInstanceMap instanceMap) {
        Object fieldValue = ReflectUtils.get(model, reflectField);
        if(fieldValue == null) {
            if(field.isNullable()) {
                return null;
            }
            throw new InternalException("Field " + fieldName() + " can not be null");
        }
        if(typeCategory.isPrimitive()) {
            return fieldValue;
        }
        return instanceMap.getInstance(fieldValue);
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public PojoDef<?> getDeclaringTypeDef() {
        return declaringTypeDef;
    }

    private Object convertValue(Object value, java.lang.reflect.Type valueType, ModelInstanceMap modelInstanceMap) {
        if(value == null) {
            if(field.isNullable()) {
                return null;
            }
            throw new InternalException("Field " + fieldName() + " can not be null");
        }
        if(typeCategory.isPrimitive()) {
            return value;
        }
        if(typeCategory.isReference()) {
            return modelInstanceMap.getModel(Object.class, (Instance) value);
        }
        throw new InternalException("Invalid type category " + typeCategory);
    }

    private String fieldName() {
        return reflectField.getDeclaringClass().getName() + "." + reflectField.getName();
    }

    public java.lang.reflect.Field getReflectField() {
        return reflectField;
    }

    public tech.metavm.object.meta.Field getField() {
        return field;
    }

    public String getName() {
        return field.getName();
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public ModelDef<?, ?> getTargetDef() {
        return targetDef;
    }
}
