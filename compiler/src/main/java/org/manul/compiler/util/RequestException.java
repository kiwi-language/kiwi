package org.manul.compiler.util;

import lombok.Getter;
import org.manul.common.ErrorCode;

@Getter
public class RequestException extends RuntimeException {

    private final ErrorCode errorCode;

    public RequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
