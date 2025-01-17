package org.metavm.instance.core;

import org.metavm.entity.EntityIdProvider;

import java.util.List;

public class CompilerIdService implements EntityIdProvider {

    public CompilerIdService() {
        super();
    }

    @Override
    public List<Long> allocate(long appId, int count) {
        throw new UnsupportedOperationException();
    }
}
