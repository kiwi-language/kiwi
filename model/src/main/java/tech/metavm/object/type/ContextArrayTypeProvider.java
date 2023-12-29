package tech.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;

public class ContextArrayTypeProvider implements ArrayTypeProvider{

    private final IEntityContext entityContext;

    public ContextArrayTypeProvider(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public ArrayType getArrayType(Type elementType, ArrayKind kind, @Nullable Long tmpId) {
        return entityContext.getArrayTypeContext(kind).get(elementType, tmpId);
    }
}
