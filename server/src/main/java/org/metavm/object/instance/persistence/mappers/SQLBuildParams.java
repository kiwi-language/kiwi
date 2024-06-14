package org.metavm.object.instance.persistence.mappers;

public record SQLBuildParams(
        boolean forDeleting,
        boolean byModelId,
        int numItems,
        boolean selectingForCount,
        boolean withLimit
) {

    public boolean forSelecting() {
        return !forDeleting;
    }

}
