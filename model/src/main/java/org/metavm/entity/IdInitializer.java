package org.metavm.entity;

import org.metavm.object.instance.core.Instance;

import java.util.Collection;

public interface IdInitializer {

    void initializeIds(long appId, Collection<? extends Instance> instances);

}
