package org.metavm.ddl;

import org.metavm.api.EntityType;

@EntityType
public enum FieldChangeKind {
    CREATION,
    TYPE_CHANGE,
    SUPER_CLASS_ADDED,
}
