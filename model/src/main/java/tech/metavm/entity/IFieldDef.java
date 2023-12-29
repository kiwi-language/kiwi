package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.util.ReflectionUtils;

public interface IFieldDef {

    default void setModelField(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        ReflectionUtils.set(model, getJavaField(), getModelFieldValue(instance, objectInstanceMap));
    }

    Object getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap);

    Instance getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap);

    @JsonIgnore
    @SuppressWarnings("unused")
    PojoDef<?> getDeclaringTypeDef();

    java.lang.reflect.Field getJavaField();

    tech.metavm.object.type.Field getField();

    String getName();
}
