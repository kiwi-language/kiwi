package tech.metavm.util;

import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.InternalErrorCode;
import tech.metavm.dto.Result;

public class ResultUtil {

    public static String formatMessage(ErrorCode errorCode, Object[] params) {
        return formatMessage(errorCode.message(), params);
    }

    public static String formatMessage(InternalErrorCode errorCode, Object[] params) {
        return formatMessage(errorCode.message(), params);
    }

    public static String formatMessage(String messageTemplate, Object[] params) {
        String message = messageTemplate;
        if(params != null) {
            for (Object param : params) {
                message = message.replaceFirst("\\{}", NncUtils.toString(param));
            }
        }
        return message;
    }

}
