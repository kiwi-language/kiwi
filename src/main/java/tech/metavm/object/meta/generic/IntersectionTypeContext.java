package tech.metavm.object.meta.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.IntersectionType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UnionType;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class IntersectionTypeContext extends CompositeTypeContext<IntersectionType>  {
    public IntersectionTypeContext(IEntityContext context, @Nullable IntersectionTypeContext parent) {
        super(context, IntersectionType.KEY_IDX, parent);
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.isEmpty()) {
            throw new InternalException("Union type must have at least one member type");
        }
    }

    @Override
    protected String getKey(List<Type> componentTypes) {
        return IntersectionType.getKey(componentTypes);
    }

    @Override
    protected boolean componentTypesEquals(List<Type> types1, List<Type> types2) {
        return new HashSet<>(types1).equals(new HashSet<>(types2));
    }

    @Override
    protected IntersectionType create(List<Type> componentTypes, Long tmpId) {
        return new IntersectionType(tmpId, new HashSet<>(componentTypes));
    }
}
