package tech.metavm.object.type.rest.dto;

import tech.metavm.common.Request;

public final class GetTypeRequest extends Request {
    private final long id;
    private final boolean includingPropertyTypes;

    public GetTypeRequest(long id, boolean includingPropertyTypes) {
        this.id = id;
        this.includingPropertyTypes = includingPropertyTypes;
    }

    public long getId() {
        return id;
    }

    public boolean isIncludingPropertyTypes() {
        return includingPropertyTypes;
    }
}
