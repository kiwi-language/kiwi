package tech.metavm.object.instance;

import tech.metavm.entity.ModelDef;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.EnumConstantRT;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;

public interface ModelInstanceMap {

    Instance getInstance(Object model);

    <T> T getEntity(Class<T> klass, Instance instance);

    <T> T getEntity(Class<T> klass, Instance instance, @Nullable ModelDef<T,?> def);

    default ClassType getClassType(Instance instance) {
        return getEntity(ClassType.class, instance);
    }

    default Type getType(Instance instance) {
        return getEntity(Type.class, instance);
    }

    default Field getField(Instance instance) {
        return getEntity(Field.class, instance);
    }

    default EnumConstantRT getEnumConstant(Instance instance) {
        return getEntity(EnumConstantRT.class, instance);
    }

}
