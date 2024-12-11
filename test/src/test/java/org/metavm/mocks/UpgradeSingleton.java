package org.metavm.mocks;

import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;

@Entity(since = 1)
public class UpgradeSingleton extends org.metavm.entity.Entity {

    public static final IndexDef<UpgradeSingleton> IDX_ALL_FLAGS = IndexDef.createUnique(UpgradeSingleton.class, "allFlags");

    private final boolean allFlags = true;

}
