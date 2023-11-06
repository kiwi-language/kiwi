package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.util.ReflectUtils;

public interface IFieldDef {

    default void setModelField(Object model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        ReflectUtils.set(model, getJavaField(), getModelFieldValue(instance, modelInstanceMap));
    }

    Object getModelFieldValue(ClassInstance instance, ModelInstanceMap modelInstanceMap);

    Instance getInstanceFieldValue(Object model, ModelInstanceMap instanceMap);

    @JsonIgnore
    @SuppressWarnings("unused")
    PojoDef<?> getDeclaringTypeDef();

    java.lang.reflect.Field getJavaField();

    tech.metavm.object.meta.Field getField();

    String getName();
}
