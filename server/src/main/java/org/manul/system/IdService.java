package org.manul.system;

import org.manul.entity.EntityIdProvider;
import org.manul.context.Component;
import org.manul.context.sql.Transactional;

import java.util.List;

@Component(module = "persistent")
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
