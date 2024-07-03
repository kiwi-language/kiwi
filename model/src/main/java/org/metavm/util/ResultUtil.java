package org.metavm.util;

import org.metavm.common.ErrorCode;

import java.util.Objects;

public class ResultUtil {

    public static String formatMessage(ErrorCode errorCode, Object... params) {
        return formatMessage(errorCode.message(), params);
    }

    public static String formatMessage(String messageTemplate, Object[] params) {
        String message = messageTemplate;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replaceFirst("\\{}", "{" + i + "}");
            }
            for (int i = 0; i < params.length; i++) {
                message = message.replace("{" + i + "}", Objects.toString(params[i]));
            }
        }
        return message;
    }

    public static void main(String[] args) {
        String msg = "{} {}";
        System.out.println(
                msg.replaceFirst("\\{}", "{0}")
                        .replaceFirst("\\{}", "{1}")
        );
    }

}
