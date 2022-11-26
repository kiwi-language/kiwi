package tech.metavm.object.instance;

import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

public interface ModelMap {

    <T> T get(Class<T> klass, IInstance instance);

    default Type getType(IInstance instance) {
        return get(Type.class, instance);
    }

    default Field getField(IInstance instance) {
        return get(Field.class, instance);
    }

    default EnumConstantRT getEnumConstant(IInstance instance) {
        return get(EnumConstantRT.class, instance);
    }

}
