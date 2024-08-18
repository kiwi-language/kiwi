package org.metavm.instance.core;

import org.metavm.entity.EntityIdProvider;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Map;

public class CompilerIdService implements EntityIdProvider {

    public CompilerIdService() {
        super();
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<? extends Type, Integer> typeId2count) {
        throw new UnsupportedOperationException();
    }
}
