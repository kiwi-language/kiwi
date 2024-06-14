package org.metavm.object.type.rest.dto;

import org.metavm.common.Request;

public final class GetTypeRequest extends Request {
    private final String id;
    private final boolean includingPropertyTypes;

    public GetTypeRequest(String id, boolean includingPropertyTypes) {
        this.id = id;
        this.includingPropertyTypes = includingPropertyTypes;
    }

    public String getId() {
        return id;
    }

    public boolean isIncludingPropertyTypes() {
        return includingPropertyTypes;
    }
}
