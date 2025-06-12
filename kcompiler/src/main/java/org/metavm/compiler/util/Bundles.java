package org.metavm.compiler.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Bundles {

    public static final String msgBundleName = "message.Messages";
    public static final Map<Locale, ResourceBundle> bundles = new HashMap<>();

    public static ResourceBundle getMessageBundle(Locale locale) {
        return bundles.computeIfAbsent(locale, l -> ResourceBundle.getBundle(msgBundleName, locale));
    }

}
