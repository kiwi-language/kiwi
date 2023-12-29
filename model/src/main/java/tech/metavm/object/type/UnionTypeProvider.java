package tech.metavm.object.type;

import javax.annotation.Nullable;
import java.util.Set;

public interface UnionTypeProvider {

    UnionType getUnionType(Set<Type> types, @Nullable Long tmpId);

    default UnionType getUnionType(Set<Type> types) {
        return getUnionType(types, null);
    }

}
