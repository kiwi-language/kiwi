package org.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TypeFactory {

    public static final Logger logger = LoggerFactory.getLogger(TypeFactory.class);

    public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
        throw new UnsupportedOperationException();
    }

}
