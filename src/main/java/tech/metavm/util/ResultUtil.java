package tech.metavm.util;

import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Result;

public class ResultUtil {

    public static String formatMessage(ErrorCode resultType, Object[] params) {
        String message = resultType.message();
        if(params != null) {
            for (Object param : params) {
                message = message.replaceFirst("\\{}", NncUtils.toString(param));
            }
        }
        return message;
    }

}
