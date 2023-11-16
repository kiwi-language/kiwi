package tech.metavm.object.type;

import tech.metavm.entity.EntityType;

@EntityType("元数据状态")
public enum MetadataState {
    INITIALIZING,
    READY,
    DELETING
}
