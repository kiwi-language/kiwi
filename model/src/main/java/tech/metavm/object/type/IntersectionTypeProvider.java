package tech.metavm.object.type;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface IntersectionTypeProvider {

    IntersectionType getIntersectionType(Set<Type> types, @Nullable Long tmpId);

    default IntersectionType getIntersectionType(Set<Type> types) {
        return getIntersectionType(types, null);
    }

}
