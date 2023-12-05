package tech.metavm.util;

import tech.metavm.common.ErrorCode;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class NamingUtils {

    public static final String[] SPECIAL_CHARACTERS = new String[] {
            " ", "\b", "\t", "\n", "\r"/*, "$"*/
    };

    private static final Pattern TYPE_CODE_PTN = Pattern.compile("^[a-zA-Z_$][\\.a-zA-Z_$0-9<>\\[\\]]*$");

    private static final Pattern CODE_PTN = Pattern.compile("^[a-zA-Z_<$][a-zA-Z_$0-9]*>?$");

    public static String ensureValidCode(@Nullable String code) {
        return ensureValidCode(code, CODE_PTN);
    }

    public static String ensureValidTypeCode(@Nullable String code) {
        return ensureValidCode(code, TYPE_CODE_PTN);
    }

    private static String ensureValidCode(@Nullable String code, Pattern pattern) {
        if(code == null || NncUtils.isBlank(code))
            return null;
        if(pattern.matcher(code).matches())
            return code;
        else
            throw new BusinessException(ErrorCode.INVALID_CODE, code);
    }


    public static String ensureValidName(String name) {
        if(NncUtils.isBlank(name))
            throw BusinessException.invalidName(name);
        for (String s : SPECIAL_CHARACTERS) {
            if(name.contains(s))
                throw BusinessException.invalidName(name);
        }
        return name;
    }
}
