package org.metavm.system;

import org.metavm.entity.EntityIdProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class IdService implements EntityIdProvider {

    private final IdGenerator idGenerator;

    public IdService(IdGenerator idGenerator) {
        super();
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Long allocate(long appId) {
        var result = allocate(appId, 1);
        return result.getFirst();
    }

    @Transactional
    public List<Long> allocate(long appId, int count) {
        return idGenerator.generate(count);
    }

}
