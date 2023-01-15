package tech.metavm.object.instance;

import tech.metavm.entity.ModelDef;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.Field;

public interface ModelInstanceMap {

    Instance getInstance(Object model);

    <T> T getModel(Class<T> klass, Instance instance);

    <T> T getModel(Class<T> klass, Instance instance, ModelDef<?,?> def);

    default ClassType getType(Instance instance) {
        return getModel(ClassType.class, instance);
    }

    default Field getField(Instance instance) {
        return getModel(Field.class, instance);
    }

    default EnumConstantRT getEnumConstant(Instance instance) {
        return getModel(EnumConstantRT.class, instance);
    }

}
