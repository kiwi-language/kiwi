package tech.metavm.object.instance;

import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;

public interface ModelMap {

    <T> T get(Class<T> klass, Instance instance);

    default Type getType(Instance instance) {
        return get(Type.class, instance);
    }

    default Field getField(Instance instance) {
        return get(Field.class, instance);
    }

    default EnumConstantRT getEnumConstant(Instance instance) {
        return get(EnumConstantRT.class, instance);
    }

}
