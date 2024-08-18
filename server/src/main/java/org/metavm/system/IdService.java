package org.metavm.system;

import org.metavm.entity.EntityIdProvider;
import org.metavm.object.type.Type;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class IdService implements EntityIdProvider {

    private final IdGenerator idGenerator;

    public IdService(IdGenerator idGenerator) {
        super();
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Long allocate(long appId, Type type) {
        var result = allocate(appId, Map.of(type, 1));
        return result.values().iterator().next().get(0);
    }

    @Transactional
    public Map<Type, List<Long>> allocate(long appId, Map<? extends Type, Integer> typeId2count) {
//        return allocate0(appId, typeId2count, 0);
        var type2ids = new HashMap<Type, List<Long>>();
        typeId2count.forEach((type, cnt) -> type2ids.put(type, idGenerator.generate(cnt)));
        return type2ids;
    }

}
