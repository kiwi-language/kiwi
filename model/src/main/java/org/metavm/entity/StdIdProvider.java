package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.StdAllocators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdIdProvider {

    public static final Logger logger = LoggerFactory.getLogger(StdIdProvider.class);
    private final StdAllocators allocators;

    public StdIdProvider(StdAllocators allocators) {
        this.allocators = allocators;
    }

    public Id getId(ModelIdentity modelIdentity) {
        return allocators.getId(modelIdentity);
    }

}
