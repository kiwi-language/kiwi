package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;

@EntityType("元数据状态")
public enum MetadataState {
    INITIALIZING,
    READY,
    DELETING
}
