package tech.metavm.flow.rest;

import javax.annotation.Nullable;

public record CopyNodeParam(
    ValueDTO source,
    @Nullable ParentRefDTO parentRef
) {
}
