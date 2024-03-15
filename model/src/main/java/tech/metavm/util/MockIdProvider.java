package tech.metavm.util;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.instance.core.TypeTag;
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
    private final Map<Id, TypeId> id2typeId = new HashMap<>();

    @Override
    public TypeId getTypeId(Id id) {
        return NncUtils.requireNonNull(id2typeId.get(id),
                () -> new InternalException("Can not find a type for id: " + id));
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count) {
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
        long resultId = nextIdMap.compute(category, (c, id) -> id == null ?
                requireNonNull(category.getIdRegion(), "region not found for category " + category).start() + INITIAL_NEXT_ID :
                id + 1
        );
        var id = PhysicalId.of(resultId, type);
        id2typeId.put(id, new TypeId(TypeTag.fromCategory(type.getCategory()), type.getId().getPhysicalId()));
        return resultId;
    }

    public void clear() {
        nextIdMap.clear();
        id2typeId.clear();
    }

    public MockIdProvider copy() {
        var copy = new MockIdProvider();
        copy.nextIdMap.putAll(nextIdMap);
        copy.id2typeId.putAll(id2typeId);
        return copy;
    }

}
