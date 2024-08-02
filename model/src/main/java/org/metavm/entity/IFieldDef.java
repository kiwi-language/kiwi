package org.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.ReflectionUtils;

public interface IFieldDef {

    default void setModelField(Object model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        ReflectionUtils.set(model, getJavaField(), getModelFieldValue(instance, objectInstanceMap));
    }

    Object getModelFieldValue(ClassInstance instance, ObjectInstanceMap objectInstanceMap);

    Value getInstanceFieldValue(Object model, ObjectInstanceMap instanceMap);

    @JsonIgnore
    @SuppressWarnings("unused")
    PojoDef<?> getDeclaringTypeDef();

    java.lang.reflect.Field getJavaField();

    org.metavm.object.type.Field getField();

    String getName();
}
