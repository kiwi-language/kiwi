package tech.metavm.lang;

import java.util.regex.Pattern;

public class RegexUtils {

    public static boolean match(String pattern, String str) {
        return Pattern.compile(pattern).matcher(str).matches();
    }

}
