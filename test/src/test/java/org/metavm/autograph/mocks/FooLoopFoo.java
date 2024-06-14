package org.metavm.autograph.mocks;

import java.util.Objects;

public class FooLoopFoo {

    public int test() {
        for (int i = 0; i < 10; i++) {

        }
        for (int i = 0, j = 0; i < 10 && j < 10; i++, j++) {}
        var i = 2;
        {
            return i;
        }
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
