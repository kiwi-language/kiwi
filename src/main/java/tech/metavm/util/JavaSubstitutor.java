package tech.metavm.util;


import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public interface JavaSubstitutor {

    Type substitute(Type type);

    Map<TypeVariable<?>, Type> toMap();

    JavaSubstitutor merge(JavaSubstitutor that);

    JavaSubstitutor merge(Map<TypeVariable<?>, Type> map);

}
