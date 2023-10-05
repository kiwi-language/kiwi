package tech.metavm.object.instance;

import tech.metavm.entity.ModelDef;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import javax.annotation.Nullable;

public interface ModelInstanceMap {

    Instance getInstance(Object model);

    <T> T getModel(Class<T> klass, Instance instance);

    <T> T getModel(Class<T> klass, Instance instance, @Nullable ModelDef<?,?> def);

    default ClassType getClassType(Instance instance) {
        return getModel(ClassType.class, instance);
    }

    default Type getType(Instance instance) {
        return getModel(Type.class, instance);
    }

    default Field getField(Instance instance) {
        return getModel(Field.class, instance);
    }

    default EnumConstantRT getEnumConstant(Instance instance) {
        return getModel(EnumConstantRT.class, instance);
    }

}
