package tech.metavm.util;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class BiUnion<T1, T2> implements RuntimeGeneric {

    public static Type createNullableType(Type type) {
        return createType(type, Null.class);
    }

    public static Type createType(Type type1, Type type2) {
        if (type1.getTypeName().compareTo(type2.getTypeName()) > 0) {
            var tmp = type2;
            type2 = type1;
            type1 = tmp;
        }
        return ParameterizedTypeImpl.create(BiUnion.class, type1, type2);
    }


    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException();
    }
}
