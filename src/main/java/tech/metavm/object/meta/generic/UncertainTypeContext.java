package tech.metavm.object.meta.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UncertainType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

public class UncertainTypeContext extends CompositeTypeContext<UncertainType> {

    public UncertainTypeContext(IEntityContext context) {
        super(context, UncertainType.KEY_IDX);
    }

    public UncertainType get(Type lowerBound, Type upperBound) {
        return get(List.of(lowerBound, upperBound));
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.size() != 2) {
            throw new InternalException("Uncertain type must have exactly two component types (upperBound & lowerBound");
        }
    }

    @Override
    protected UncertainType create(List<Type> componentTypes) {
        NncUtils.requireTrue(componentTypes.size() == 2);
        return new UncertainType(null, componentTypes.get(0), componentTypes.get(1));
    }
}
