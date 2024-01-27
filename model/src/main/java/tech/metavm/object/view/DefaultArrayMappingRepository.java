package tech.metavm.object.view;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.type.ArrayType;

public class DefaultArrayMappingRepository implements ArrayMappingRepository {

    private final IEntityContext entityContext;

    public DefaultArrayMappingRepository(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Nullable
    @Override
    public ArrayMapping get(ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping) {
        return entityContext.selectFirstByKey(ArrayMapping.IDX, sourceType, targetType, elementMapping);
    }

    @Override
    public void add(ArrayMapping arrayMapping) {
        entityContext.bind(arrayMapping);
    }
}
