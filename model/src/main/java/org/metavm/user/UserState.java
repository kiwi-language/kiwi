package org.metavm.user;

import org.metavm.entity.EntityType;

@EntityType
public enum UserState {
    ACTIVE,
    INACTIVE,
    DETACHED
}
