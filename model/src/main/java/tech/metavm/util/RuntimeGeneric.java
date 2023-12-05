package tech.metavm.util;


import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

public interface RuntimeGeneric {

    default Type getGenericType() {
        Class<?> klass = getClass();
        if(klass.getTypeParameters().length == 0) {
            return klass;
        }
        Map<TypeVariable<?>, Type> typeVariableTypeMap = getTypeVariableMap();
        List<Type> typeArgs = NncUtils.map(
                klass.getTypeParameters(),
                p -> NncUtils.requireNonNull(typeVariableTypeMap.get(p), "Can not resolve " + p)
        );
        return ParameterizedTypeImpl.create(klass, typeArgs);
    }

    Map<TypeVariable<?>, Type> getTypeVariableMap();

}
