package org.manul.entity;

import org.manul.object.type.Klass;

public interface StdKlassBuilder {

    Klass build(StdKlassRegistry registry);

    Class<?> getJavaClass();

}
