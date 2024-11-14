package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metavm.common.Request;

public final class GetTypeByCodeRequest extends Request {
    private final String qualifiedName;

    public GetTypeByCodeRequest(@JsonProperty("qualifiedName") String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

}
