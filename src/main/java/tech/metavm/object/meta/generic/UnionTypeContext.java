package tech.metavm.object.meta.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UnionType;
import tech.metavm.util.InternalException;

import java.util.HashSet;
import java.util.List;

public class UnionTypeContext extends CompositeTypeContext<UnionType>  {
    public UnionTypeContext(IEntityContext context) {
        super(context, UnionType.KEY_IDX);
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.isEmpty()) {
            throw new InternalException("Union type must have at least one member type");
        }
    }

    @Override
    protected String getKey(List<Type> componentTypes) {
        return UnionType.getKey(componentTypes);
    }

    @Override
    protected boolean componentTypesEquals(List<Type> types1, List<Type> types2) {
        return new HashSet<>(types1).equals(new HashSet<>(types2));
    }

    @Override
    protected UnionType create(List<Type> componentTypes) {
        return new UnionType(null, new HashSet<>(componentTypes));
    }
}
