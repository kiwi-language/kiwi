package org.metavm.autograph;

public enum AccessMode {
    READ(true, false, false),
    WRITE(false, true, false),
    READ_WRITE(true, true, false),
    DEFINE(false, false, true),
    DEFINE_WRITE(false, true, true),

    ;

    final boolean isDefinition;
    final boolean isRead;
    final boolean isWrite;

    AccessMode(boolean isRead, boolean isWrite, boolean isDefinition) {
        this.isRead = isRead;
        this.isWrite = isWrite;
        this.isDefinition = isDefinition;
    }

    public boolean isDefinition() {
        return isDefinition;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isWrite() {
        return isWrite;
    }
}
