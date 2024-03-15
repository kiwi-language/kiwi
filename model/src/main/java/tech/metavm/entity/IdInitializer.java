package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;

import java.util.Collection;

public interface IdInitializer {

    TypeId getTypeId(Id id);

    void initializeIds(Id appId, Collection<? extends DurableInstance> instances);

}
