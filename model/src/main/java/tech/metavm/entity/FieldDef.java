package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.ReflectionUtils;

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
    public void setModelField(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Object fieldValue = getModelFieldValue(instance, objectInstanceMap);
        ReflectionUtils.set(model, javaField, fieldValue);
    }

    @Override
    public Object getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Instance fieldValue = instance.getField(field);
        if (targetDef instanceof InstanceDef<?> || targetDef instanceof InstanceCollectionDef<?, ?>) {
//            noinspection rawtypes,unchecked
            return objectInstanceMap.getEntity(javaField.getType(), fieldValue, (ModelDef) targetDef);
        }
        return objectInstanceMap.getEntity(javaField.getType(), fieldValue);
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap) {
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
