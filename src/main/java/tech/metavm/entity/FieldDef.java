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
    private final tech.metavm.object.type.Field field;
    private final ModelDef<?, ?> targetDef;

    public FieldDef(tech.metavm.object.type.Field field,
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
        if(targetDef instanceof InstanceDef<?> || targetDef instanceof InstanceCollectionDef<?,?>) {
            return modelInstanceMap.getModel(javaField.getType(), fieldValue, targetDef);
        }
        return modelInstanceMap.getModel(javaField.getType(), fieldValue);
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ModelInstanceMap instanceMap) {
        EntityUtils.ensureProxyInitialized(model);
        return instanceMap.getInstance(ReflectUtils.get(model, javaField));
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
    public tech.metavm.object.type.Field getField() {
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
