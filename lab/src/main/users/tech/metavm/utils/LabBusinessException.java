package tech.metavm.utils;

import tech.metavm.entity.EntityType;

import java.util.List;
import java.util.Objects;

@EntityType("业务异常")
public class LabBusinessException extends RuntimeException {

    public LabBusinessException(LabErrorCode errorCode, List<Object> params) {
        super(formatMessage(errorCode.message(), params));
    }

    private static String formatMessage(String messageTemplate, List<Object> params) {
        String message = messageTemplate;
        if(params != null) {
            for (int i = 0; i < params.size(); i++) {
                message = message.replaceFirst("\\{}", "{" + i + "}");
            }
            for (int i = 0; i < params.size(); i++) {
                message = message.replace("{" + i + "}", Objects.toString(params.get(i)));
            }
        }
        return message;
    }

}
