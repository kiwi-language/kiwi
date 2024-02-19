package tech.metavm.object.type.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.UnionType;
import tech.metavm.object.type.UnionTypeProvider;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnionTypeContext extends CompositeTypeContext<UnionType> implements UnionTypeProvider {
    public UnionTypeContext(IEntityContext context, @Nullable UnionTypeContext parent) {
        super(context, UnionType.KEY_IDX, parent);
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.isEmpty()) {
            throw new InternalException("Union type must have at least one member type");
        }
    }

    public UnionType getUnionType(Set<Type> members) {
        return getUnionType(members, null);
    }

    public UnionType getUnionType(Set<Type> members, Long tmpId) {
        return get(new ArrayList<>(members), tmpId);
    }

    @Override
    protected String getKey(List<Type> componentTypes) {
        return UnionType.getKey(componentTypes);
    }

    @Override
    protected Object getMemKey(List<Type> types) {
        return new HashSet<>(types);
    }

    @Override
    protected UnionType create(List<Type> componentTypes, Long tmpId) {
        return new UnionType(tmpId, new HashSet<>(componentTypes));
    }
}
