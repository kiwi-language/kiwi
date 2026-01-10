package org.manul.util;

import org.manul.common.ErrorCode;
import org.manul.entity.natives.EmailSender;

import java.util.regex.Pattern;

public class EmailUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    public static EmailSender emailSender;

    public static boolean isEmailAddress(String str) {
        return EMAIL_PATTERN.matcher(str).matches();
    }

    public static void ensureEmailAddress(String str) {
        if(!isEmailAddress(str))
            throw new BusinessException(ErrorCode.INVALID_EMAIL_ADDRESS);
    }

}
