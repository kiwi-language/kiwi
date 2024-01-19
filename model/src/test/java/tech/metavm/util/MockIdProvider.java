package tech.metavm.util;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.NncUtils.requireNonNull;

public class MockIdProvider implements EntityIdProvider {

    public static final long INITIAL_NEXT_ID = 1000000L;
    private final Map<TypeCategory, Long> nextIdMap = new HashMap<>();
    private final Map<Long, Type> id2type = new HashMap<>();

    @Override
    public long getTypeId(long id) {
        return NncUtils.requireNonNull(
                id2type.get(id),
                () -> new InternalException("Can not find a type for id: " + id)
        ).tryGetId();
    }

    public Type getType(long id) {
        return id2type.get(id);
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count) {
        Map<Type, List<Long>> result = new HashMap<>();
        typeId2count.forEach((type, count) -> {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                long id = allocateOne(type);
                ids.add(id);
            }
            result.put(type, ids);
        });

        return result;
    }

    private long allocateOne(Type type) {
        TypeCategory category = type.getCategory();
        long resultId = nextIdMap.compute(category, (c, id) -> id == null ?
                requireNonNull(category.getIdRegion(), "region not found for category " + category).start() + INITIAL_NEXT_ID :
                id + 1
        );
        id2type.put(resultId, type);
        return resultId;
    }

    public void clear() {
        nextIdMap.clear();
        id2type.clear();
    }

}
