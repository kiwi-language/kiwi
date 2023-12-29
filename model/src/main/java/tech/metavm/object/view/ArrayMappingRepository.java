package tech.metavm.object.view;

import tech.metavm.object.type.ArrayType;

import javax.annotation.Nullable;

public interface ArrayMappingRepository {

    @Nullable ArrayMapping get(ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping);

    void add(ArrayMapping arrayMapping);

}
