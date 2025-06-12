package org.metavm.compiler.diag;

import org.metavm.compiler.util.Bundles;

import java.util.Locale;

public class Messages {


    public static final Messages instance = new Messages();

    public String getMessage(Locale locale, String key) {
        var bundle = Bundles.getMessageBundle(locale);
        return bundle.getString(key);
    }

}
