package tech.metavm.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class BiUnion<T1, T2> implements RuntimeGeneric {

    public static Type createNullableType(Type type) {
        return createType(type, Null.class);
    }

    public static boolean isNullable(Type type) {
        if (type instanceof ParameterizedType pType) {
            return pType.getRawType() == BiUnion.class &&
                    (pType.getActualTypeArguments()[0] == Null.class
                            || pType.getActualTypeArguments()[1] == Null.class);
        }
        return false;
    }

    public static Type getUnderlyingType(Type type) {
        if (type instanceof ParameterizedType pType) {
            if (pType.getRawType() == BiUnion.class) {
                if (pType.getActualTypeArguments()[0] == Null.class)
                    return pType.getActualTypeArguments()[1];
                else if (pType.getActualTypeArguments()[1] == Null.class)
                    return pType.getActualTypeArguments()[0];
            }
        }
        throw new IllegalArgumentException("Type " + type + " is not a nullable type");
    }

    public static Type createType(Type type1, Type type2) {
        if (type1.equals(type2))
            throw new IllegalArgumentException("The member types must be distinct");
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
