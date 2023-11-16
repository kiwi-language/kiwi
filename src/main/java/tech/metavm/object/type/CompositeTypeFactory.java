package tech.metavm.object.type;

import java.util.Set;

public interface CompositeTypeFactory {

    UnionType getUnionType(Set<Type> members);

}
