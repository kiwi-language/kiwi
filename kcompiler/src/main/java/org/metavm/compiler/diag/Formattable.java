package org.metavm.compiler.diag;

import java.util.Locale;

public interface Formattable {

    String toString(Locale locale, Messages messages);

}
