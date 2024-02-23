package tech.metavm.util;

import tech.metavm.common.ErrorCode;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class NamingUtils {

    public static final String[] SPECIAL_CHARACTERS = new String[]{
            " ", "\b", "\t", "\n", "\r"/*, "$"*/
    };

    private static final Pattern TYPE_CODE_PATTERN = Pattern.compile("^[\\[\\(a-zA-Z_$][&\\|,\\(\\)\\-\\.a-zA-Z_$0-9<>\\[\\]]*$");

    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z_<$][a-zA-Z_$0-9]*>?$");

    public static String ensureValidCode(@Nullable String code) {
        return ensureValidCode(code, CODE_PATTERN);
    }

    public static String ensureValidTypeCode(@Nullable String code) {
        return ensureValidCode(code, TYPE_CODE_PATTERN);
    }

    public static String ensureValidFlowCode(@Nullable String code) {
        if (code == null || NncUtils.isBlank(code))
            return null;
        code = removeSpaces(code);
        if (TYPE_CODE_PATTERN.matcher(code).matches() || CODE_PATTERN.matcher(code).matches())
            return code;
        else
            throw new BusinessException(ErrorCode.INVALID_CODE, code);
    }

    private static String ensureValidCode(@Nullable String code, Pattern pattern) {
        if (code == null || NncUtils.isBlank(code))
            return null;
        code = removeSpaces(code);
        if (pattern.matcher(code).matches())
            return code;
        else
            throw new BusinessException(ErrorCode.INVALID_CODE, code);
    }


    public static String ensureValidName(String name) {
        name = removeSpaces(name);
        if (NncUtils.isBlank(name))
            throw BusinessException.invalidName(name);
        for (String s : SPECIAL_CHARACTERS) {
            if (name.contains(s))
                throw BusinessException.invalidName(name);
        }
        return name;
    }

    public static String escapeTypeName(String name) {
        int idx = name.lastIndexOf('.');
        if (idx >= 0)
            name = name.substring(idx + 1);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            var c = name.charAt(i);
            if (c == '<')
                buf.append("__");
            else if (c == ',')
                buf.append('_');
            else if (c == '>')
                buf.append("__");
            else if (c == '[' || c == ']')
                buf.append('$');
            else
                buf.append(c);
        }
        return buf.toString();
    }

    public static @Nullable String getGetterName(@Nullable String code) {
        return tryAddPrefix(code, "get");
    }

    public static @Nullable String getSetterName(@Nullable String code) {
        return tryAddPrefix(code, "set");
    }

    public static @Nullable String tryAddPrefix(@Nullable String code, String prefix) {
        return code != null ? prefix + firstCharToUpperCase(escapeTypeName(code)) : null;
    }

    private static String removeSpaces(String str) {
        return str.replace(" ", "");
    }

    public static String firstCharToUpperCase(String s) {
        if (s.isEmpty())
            return "";
        if (s.length() == 1)
            return s.toUpperCase();
        else
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String firstCharToLowerCase(String s) {
        if (s.isEmpty()) {
            return s;
        }
        if (s.length() == 1) {
            return s.toLowerCase();
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }


}
