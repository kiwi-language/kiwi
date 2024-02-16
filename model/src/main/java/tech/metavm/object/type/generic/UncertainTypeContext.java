package tech.metavm.object.type.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.UncertainType;
import tech.metavm.object.type.UncertainTypeProvider;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class UncertainTypeContext extends CompositeTypeContext<UncertainType> implements UncertainTypeProvider {

    public UncertainTypeContext(IEntityContext context, @Nullable UncertainTypeContext parent) {
        super(context, UncertainType.KEY_IDX, parent);
    }

    public UncertainType get(Type lowerBound, Type upperBound) {
        return getUncertainType(lowerBound, upperBound, null);
    }

    public UncertainType getUncertainType(Type lowerBound, Type upperBound, Long tmpId) {
        if(lowerBound instanceof UncertainType u)
            lowerBound = u.getLowerBound();
        if(upperBound instanceof UncertainType u)
            upperBound = u.getUpperBound();
        return get(List.of(lowerBound, upperBound), tmpId);
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.size() != 2) {
            throw new InternalException("Uncertain type must have exactly two component types (upperBound & lowerBound");
        }
    }

    @Override
    protected UncertainType create(List<Type> componentTypes, Long tmpId) {
        NncUtils.requireTrue(componentTypes.size() == 2);
        return new UncertainType(tmpId, componentTypes.get(0), componentTypes.get(1));
    }
}
