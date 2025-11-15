package org.metavm.entity;

import org.metavm.object.type.Klass;

public interface StdKlassBuilder {

    Klass build(StdKlassRegistry registry);

    Class<?> getJavaClass();

}
