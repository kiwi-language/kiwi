package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metavm.common.Request;

public final class GetTypeByCodeRequest extends Request {
    private final String code;

    public GetTypeByCodeRequest(@JsonProperty("code") String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
