package org.manul.compiler.util;

import lombok.Getter;

@Getter
public class RequestException extends RuntimeException {

    public RequestException(String message) {
        super(message);
    }

}
