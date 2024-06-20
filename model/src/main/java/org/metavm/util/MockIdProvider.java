package org.metavm.util;

import org.metavm.entity.EntityIdProvider;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metavm.util.NncUtils.requireNonNull;

public class MockIdProvider implements EntityIdProvider {

    public static final long INITIAL_NEXT_ID = 1000000L;
    private final Map<TypeCategory, Long> nextIdMap = new HashMap<>();

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<? extends Type, Integer> typeId2count) {
        Map<Type, List<Long>> result = new HashMap<>();
        typeId2count.forEach((type, count) -> {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                var id = allocateOne(type);
                ids.add(id);
            }
            result.put(type, ids);
        });

        return result;
    }

    private Long allocateOne(Type type) {
        TypeCategory category = type.getCategory();
        return nextIdMap.compute(category, (c, id) -> id == null ?
                requireNonNull(category.getIdRegion(), "region not found for category " + category).start() + INITIAL_NEXT_ID :
                id + 1
        );
    }

    public void clear() {
        nextIdMap.clear();
    }

    public MockIdProvider copy() {
        var copy = new MockIdProvider();
        copy.nextIdMap.putAll(nextIdMap);
        return copy;
    }

}
