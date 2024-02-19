package tech.metavm.object.type.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.type.IntersectionType;
import tech.metavm.object.type.IntersectionTypeProvider;
import tech.metavm.object.type.Type;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntersectionTypeContext extends CompositeTypeContext<IntersectionType> implements IntersectionTypeProvider {
    public IntersectionTypeContext(IEntityContext context, @Nullable IntersectionTypeContext parent) {
        super(context, IntersectionType.KEY_IDX, parent);
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.isEmpty()) {
            throw new InternalException("Union type must have at least one member type");
        }
    }

    public IntersectionType getIntersectionType(Set<Type> members, @Nullable Long tmpId) {
        return get(new ArrayList<>(members), tmpId);
    }

    @Override
    protected String getKey(List<Type> componentTypes) {
        return IntersectionType.getKey(componentTypes);
    }

    @Override
    protected Object getMemKey(List<Type> types) {
        return new HashSet<>(types);
    }

    @Override
    protected IntersectionType create(List<Type> componentTypes, Long tmpId) {
        return new IntersectionType(tmpId, new HashSet<>(componentTypes));
    }
}
