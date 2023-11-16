package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;

public class ClassFieldDef implements IFieldDef {

    private final PojoDef<?> declaringTypeDef;
    private final tech.metavm.object.type.Field field;
    private final Field javaField;
    private final DefMap defMap;

    public ClassFieldDef(PojoDef<?> declaringTypeDef,
                         tech.metavm.object.type.Field field,
                         Field javaField,
                         DefMap defMap) {
        this.declaringTypeDef = declaringTypeDef;
        this.field = field;
        this.javaField = javaField;
        this.defMap = defMap;
        declaringTypeDef.addFieldDef(this);
    }

    @Override
    public Object getModelFieldValue(ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        Instance instanceFieldValue =instance.getField(field);
        if(instanceFieldValue.isNull()) {
            return null;
        }
        ClassType type = modelInstanceMap.getModel(ClassType.class, instanceFieldValue);
        return defMap.getDef(type).getJavaClass();
    }

    @Override
    public Instance getInstanceFieldValue(Object model, ModelInstanceMap instanceMap) {
        Class<?> fieldValue = (Class<?>) ReflectUtils.get(model, javaField);
        return fieldValue != null ?
                instanceMap.getInstance(defMap.getType(fieldValue))
                : InstanceUtils.nullInstance();
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
    public tech.metavm.object.type.Field getField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }
}
