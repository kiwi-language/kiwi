package tech.metavm.object.view;

import tech.metavm.object.type.ArrayType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MockArrayMappingRepository implements ArrayMappingRepository {

    private final Map<Key, ArrayMapping> map = new HashMap<>();

    @Override
    public @Nullable ArrayMapping get(ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping) {
        return map.get(new Key(sourceType, targetType, elementMapping));
    }

    @Override
    public void add(ArrayMapping arrayMapping) {
        map.put(new Key(arrayMapping.getSourceType(), arrayMapping.getTargetType(), arrayMapping.getElementMapping()), arrayMapping);
    }

    private record Key(ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping) {
    }
}
