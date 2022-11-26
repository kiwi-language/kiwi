package tech.metavm.object.meta;

import java.util.List;
import java.util.Set;

public interface CompositeTypeFactory {

    Type getParameterizedType(Type rawType, List<Type> typeArguments);

    Type getUnionType(Set<Type> typeMembers);

}
