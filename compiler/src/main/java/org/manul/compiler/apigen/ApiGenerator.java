package org.manul.compiler.apigen;

import org.manul.compiler.element.Clazz;
import org.manul.compiler.util.List;

public interface ApiGenerator {
    long CURRENT_VERSION = 3;

    String generate(List<Clazz> rootClasses);
}
