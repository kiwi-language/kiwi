package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityType;

@EntityType
public enum PickMethod {
    ON_DEMAND,
    // 不领料
    NONE,
}
