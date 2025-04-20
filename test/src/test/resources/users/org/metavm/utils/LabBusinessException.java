package org.metavm.utils;

import org.metavm.api.Entity;

import java.util.Objects;

@Entity
public class LabBusinessException extends RuntimeException {

    public LabBusinessException(LabErrorCode errorCode, Object...params) {
        super(formatMessage(errorCode.message(), params));
    }

    private static String formatMessage(String messageTemplate, Object[] params) {
        String message = messageTemplate;
        if(params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replaceFirst("\\{}", "{" + i + "}");
            }
            for (int i = 0; i < params.length; i++) {
                message = message.replace("{" + i + "}", Objects.toString(params[i]));
            }
        }
        return message;
    }

}
