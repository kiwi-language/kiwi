package org.metavm.application;

import org.metavm.entity.EntityType;
import org.metavm.entity.EnumConstant;

@EntityType
public enum ApplicationState {
    ACTIVE,
    REMOVING,
}
