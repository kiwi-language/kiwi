package org.metavm.compiler.apigen;

import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.util.List;

public interface ApiGenerator {
    long CURRENT_VERSION = 3;

    String generate(List<Clazz> rootClasses);
}
