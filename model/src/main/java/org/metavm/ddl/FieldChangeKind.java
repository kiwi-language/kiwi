package org.metavm.ddl;

import org.metavm.api.Entity;

@Entity
public enum FieldChangeKind {
    CREATION,
    TYPE_CHANGE,
    SUPER_CLASS_ADDED,
}
