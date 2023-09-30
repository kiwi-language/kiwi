package tech.metavm.object.meta;

import java.util.Set;

public interface CompositeTypeFactory {

    UnionType getUnionType(Set<Type> members);

}
