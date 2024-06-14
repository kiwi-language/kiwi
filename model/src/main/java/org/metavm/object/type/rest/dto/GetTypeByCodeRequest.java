package org.metavm.object.type.rest.dto;

import org.metavm.common.Request;

public final class GetTypeByCodeRequest extends Request {
    private final String code;

    public GetTypeByCodeRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
