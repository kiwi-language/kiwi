package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;

import java.util.Collection;

public interface IdInitializer {

    long getTypeId(long id);

    void initializeIds(long appId, Collection<? extends DurableInstance> instances);

}
