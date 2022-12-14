package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;

public interface IFieldDef {
    void setModelField(Object model, ClassInstance instance, ModelInstanceMap modelInstanceMap);

    Object getModelFieldValue(ClassInstance instance, ModelInstanceMap modelInstanceMap);

    Instance getInstanceFieldValue(Object model, ModelInstanceMap instanceMap);

    @JsonIgnore
    @SuppressWarnings("unused")
    PojoDef<?> getDeclaringTypeDef();

    java.lang.reflect.Field getJavaField();

    tech.metavm.object.meta.Field getField();

    String getName();
}
