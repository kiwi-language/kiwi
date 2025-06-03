package org.metavm.compiler.diag;

import org.metavm.compiler.util.Bundles;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;

public class BasicDiagFmt implements DiagFmt {

    @Override
    public String format(Diag diag, Locale l) {
        var bundle = Bundles.getMessageBundle(l);
        String ptn;
        try {
            ptn = bundle.getString(diag.getCode().getKey());
        } catch (MissingResourceException e) {
            ptn = "Broken diagnostic format. arguments: {0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}";
        }
        return MessageFormat.format(ptn, diag.getArgs());
    }
}
