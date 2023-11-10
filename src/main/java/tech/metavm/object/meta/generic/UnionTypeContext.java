package tech.metavm.object.meta.generic;

import org.elasticsearch.action.search.SearchTask;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UnionType;
import tech.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public UnionType get(Set<Type> members) {
        return get(members, null);
    }

    public UnionType get(Set<Type> members, Long tmpId) {
        return get(new ArrayList<>(members), tmpId);
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
    protected UnionType create(List<Type> componentTypes, Long tmpId) {
        return new UnionType(tmpId, new HashSet<>(componentTypes));
    }
}
