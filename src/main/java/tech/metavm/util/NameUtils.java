package tech.metavm.util;

public class NameUtils {

    public static final String[] SPECIAL_CHARACTERS = new String[] {
            " ", "\b", "\t", "\n", "\r", "$"
    };

    public static String checkName(String name) {
        if(NncUtils.isBlank(name)) {
            throw BusinessException.invalidSymbolName(name);
        }
        for (String s : SPECIAL_CHARACTERS) {
            if(name.contains(s)) {
                throw BusinessException.invalidSymbolName(name);
            }
        }
        return name;
    }
}
