package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.ValueUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class FieldDef {

    private final Field reflectField;
    private final PojoDef<?> declaringTypeDef;
    private final tech.metavm.object.meta.Field field;
    private final ModelDef<?, ?> targetDef;
    private final TypeCategory typeCategory;
    private final boolean nullable;
    private final String name;
    private final boolean unique;
    private final boolean asTitle;
    private final boolean isChild;

    public FieldDef(String name,
                    tech.metavm.object.meta.Field field,
                    Field reflectField,
                    PojoDef<?> declaringTypeDef,
                    ModelDef<?, ?> targetDef) {
        this.reflectField = reflectField;
        typeCategory = ValueUtil.getTypeCategory(reflectField.getGenericType());
        if(typeCategory.isReference() && targetDef == null) {
            throw new InternalException("Can not find definition for type: " + reflectField.getGenericType());
        }
        nullable = reflectField.isAnnotationPresent(Nullable.class);
        EntityField annotation = reflectField.getAnnotation(EntityField.class);
        unique = annotation != null && annotation.unique();
        asTitle = annotation != null && annotation.asTitle();
        isChild = reflectField.isAnnotationPresent(ChildEntity.class);
        this.targetDef = targetDef;
        this.name = name != null ? name :extractFieldName(reflectField);
        this.declaringTypeDef = declaringTypeDef;
        this.field = createField(field);
        declaringTypeDef.addFieldDef(this);
    }

    private static String extractFieldName(Field field) {
        EntityField entityField = field.getAnnotation(EntityField.class);
        if(entityField != null) {
            return entityField.value();
        }
        ChildEntity childEntity = field.getAnnotation(ChildEntity.class);
        if(childEntity != null) {
            return childEntity.value();
        }
        return field.getName();
    }

    public void setField(Object entity, IInstance instance, ModelMap modelMap) {
        Object fieldValue = getFieldValue(instance, modelMap);
        ReflectUtils.set(entity, reflectField, fieldValue);
    }

    public Object getFieldValue(IInstance instance, ModelMap modelMap) {
        return convertValue(instance.getRaw(field), modelMap);
    }

    private tech.metavm.object.meta.Field createField(tech.metavm.object.meta.Field field) {
        if(field == null) {
            field = new tech.metavm.object.meta.Field(
                    name,
                    declaringTypeDef.getType(),
                    Access.GLOBAL,
                    unique,
                    asTitle,
                    null,
                    getFieldType(),
                    isChild
            );
        }
        else {
            field.setName(name);
            field.setAccess(Access.GLOBAL);
            field.setUnique(unique);
            field.setAsTitle(asTitle);
            field.setDefaultValue(null);
            field.setType(getFieldType());
            field.setChildField(isChild);
        }
        return field;
    }

    public Object getInstanceFieldValue(Object model, InstanceMap instanceMap) {
        Object fieldValue = ReflectUtils.get(model, reflectField);
        if(fieldValue == null) {
            if(nullable) {
                return null;
            }
            throw new InternalException("Field " + fieldName() + " can not be null");
        }
        IInstance existing;
        if(targetDef == null) {
            return fieldValue;
        }
        if((existing = instanceMap.getByModel(fieldValue)) != null) {
            return existing;
        }
        return getTargetRef(targetDef, fieldValue, instanceMap);
    }

    private <T> Object getTargetRef(ModelDef<T, ?> modelDef, Object model, InstanceMap instanceMap) {
        return modelDef.newInstance(modelDef.getEntityType().cast(model), instanceMap);
    }

    private Type getFieldType() {
        Type refType;
        if(typeCategory.isPrimitive()) {
            refType = ValueUtil.getPrimitiveType(reflectField.getType());
        }
        else {
            refType = targetDef.getType();
        }
        if(reflectField.isAnnotationPresent(Nullable.class)) {
            return refType.getNullableType();
        }
        else {
            return refType;
        }
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public PojoDef<?> getDeclaringTypeDef() {
        return declaringTypeDef;
    }

    private Object convertValue(Object value, ModelMap modelMap) {
        if(value == null) {
            if(nullable) {
                return null;
            }
            throw new InternalException("Field " + fieldName() + " can not be null");
        }
        if(typeCategory.isPrimitive()) {
            return value;
        }
        if(typeCategory.isReference()) {
            return modelMap.get(targetDef.getEntityType(), (IInstance) value);
        }
        throw new InternalException("Invalid type category " + typeCategory);
    }

    private <I extends Instance> Object newTargetModel(ModelDef<?, I> targetModelDef, IInstance instance, ModelMap modelMap) {
        return targetModelDef.newModelHelper(instance, modelMap);
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
        return name;
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public ModelDef<?, ?> getTargetDef() {
        return targetDef;
    }
}
