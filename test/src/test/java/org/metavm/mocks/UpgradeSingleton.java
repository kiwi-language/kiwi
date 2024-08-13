package org.metavm.mocks;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;

@EntityType(since = 1)
public class UpgradeSingleton extends Entity {

    public static final IndexDef<UpgradeSingleton> IDX_ALL_FLAGS = IndexDef.createUnique(UpgradeSingleton.class, "allFlags");

    private final boolean allFlags = true;

}
