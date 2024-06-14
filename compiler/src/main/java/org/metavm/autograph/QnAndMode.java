package org.metavm.autograph;

public record QnAndMode(QualifiedName qualifiedName, AccessMode accessMode) {

    boolean isWrite() {
        return accessMode.isWrite;
    }

    boolean isRead() {
        return accessMode.isRead;
    }

    public boolean isDefinition(){
        return accessMode.isDefinition;
    }

}
