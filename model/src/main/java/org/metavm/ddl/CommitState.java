package org.metavm.ddl;

import org.metavm.api.EntityType;

@EntityType
public enum CommitState {
    RUNNING,
    CLEANING_UP,
    FINISHED
}
