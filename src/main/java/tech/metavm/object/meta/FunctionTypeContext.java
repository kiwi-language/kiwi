package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.meta.generic.CompositeTypeContext;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class FunctionTypeContext extends CompositeTypeContext<FunctionType> {

    public FunctionTypeContext(IEntityContext context) {
        super(context, FunctionType.KEY_IDX);
    }

    public FunctionType get(List<Type> parameterTypes, Type returnType) {
        return get(NncUtils.append(parameterTypes, returnType));
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.isEmpty()) {
            throw new InternalException("FunctionType must have at least one component type (the return type)");
        }
    }

    @Override
    protected FunctionType create(List<Type> componentTypes) {
        NncUtils.requireTrue(!componentTypes.isEmpty());
        return new FunctionType(
                null,
                componentTypes.subList(0, componentTypes.size() - 1),
                componentTypes.get(componentTypes.size() - 1)
        );
    }
}
