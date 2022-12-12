package tech.metavm.entity;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;

import java.util.HashSet;
import java.util.Set;

public final class DefaultTypeResolver implements TypeResolver {

    private final Set<Long> resolving = new HashSet<>();

    @Override
    public Type getType(InstanceContext context, long typeId) {
        if (resolving.contains(typeId)) {
            throw new InternalException("Fail to resolve root Type (id:" + typeId + "). The instance of the root type " +
                    "should be preloaded.");
        }
        resolving.add(typeId);
        Type type = context.getEntityContext().getType(typeId);
        resolving.remove(typeId);
        return type;
    }
}
