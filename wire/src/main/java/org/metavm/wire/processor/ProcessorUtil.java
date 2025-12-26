package org.metavm.wire.processor;

import javax.lang.model.element.Name;

public class ProcessorUtil {
    public static String decapitalize(Name s) {
        return decapitalize(s.toString());
    }

    public static String decapitalize(String s) {
        if (s.isEmpty())
            return "";
        if (s.length() == 1)
            return s.toLowerCase();
        return Character.isUpperCase(s.charAt(1)) ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
