package tech.metavm.object.meta.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.ArrayKind;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ArrayTypeContext extends CompositeTypeContext<ArrayType>  {

    private final ArrayKind kind;

    public ArrayTypeContext(IEntityContext context, ArrayKind kind, @Nullable ArrayTypeContext parent) {
        super(context, ArrayType.KEY_IDX, parent);
        this.kind = kind;
    }

    public ArrayType get(Type elementType) {
        return get(elementType, null);
    }

    public ArrayType get(Type elementType, Long tmpId) {
        return get(List.of(elementType), tmpId);
    }

    @Override
    public void checkComponentTypes(List<Type> types) {
        if(types.size() != 1) {
            throw new InternalException("Array type can must have exactly one component type");
        }
    }

    @Override
    protected String getKey(List<Type> componentTypes) {
        return ArrayType.getKey(componentTypes.get(0), kind);
    }

    @Override
    protected ArrayType create(List<Type> componentTypes, Long tmpId) {
        NncUtils.requireTrue(componentTypes.size() == 1);
        return new ArrayType(tmpId, componentTypes.get(0), kind);
    }
}
