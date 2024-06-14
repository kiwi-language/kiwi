package org.metavm.entity;

import org.metavm.object.instance.core.DurableInstance;

import java.util.Collection;

public interface IdInitializer {

    void initializeIds(long appId, Collection<? extends DurableInstance> instances);

}
